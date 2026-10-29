package com.tattos.service;

import com.tattos.dto.response.PagamentoComissaoDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ConflictException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Arquivo;
import com.tattos.model.PagamentoComissao;
import com.tattos.model.PagamentoComissaoItem;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusRepasse;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.ArquivoRepository;
import com.tattos.repository.PagamentoComissaoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoComissaoService {

    private final PagamentoComissaoRepository pagamentoRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArquivoRepository arquivoRepository;
    private final AdiantamentoService adiantamentoService;
    private final ReciboPdfService reciboPdfService;

    @Transactional
    public PagamentoComissaoDTO criar(Long tatuadorId, List<Long> transacaoIds,
                                      String formaPagamento, Long comprovanteId) {
        Usuario tatuador = usuarioRepository.findById(tatuadorId)
                .filter(Usuario::isTatuador)
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + tatuadorId));

        if (transacaoIds == null || transacaoIds.isEmpty()) {
            throw new BusinessException("Selecione ao menos um trabalho para pagar.");
        }

        List<TransacaoFinanceira> transacoes = transacaoRepository.findAllById(transacaoIds);
        if (transacoes.size() != transacaoIds.size()) {
            throw new ResourceNotFoundException("Uma ou mais transações não foram encontradas.");
        }

        BigDecimal totalBruto = BigDecimal.ZERO;
        for (TransacaoFinanceira t : transacoes) {
            if (!TipoTransacao.RECEITA.equals(t.getTipo()) || t.getTatuador() == null
                    || !t.getTatuador().getId().equals(tatuadorId)) {
                throw new BusinessException(
                        "A transação #" + t.getId() + " não é uma receita deste tatuador.");
            }
            if (t.getStatusRepasse() != StatusRepasse.PENDENTE) {
                throw new ConflictException(
                        "A transação #" + t.getId() + " já está " + t.getStatusRepasse().name().toLowerCase()
                                + " e não pode ser selecionada novamente.");
            }
            totalBruto = totalBruto.add(t.getValorRepasse() != null ? t.getValorRepasse() : BigDecimal.ZERO);
        }

        // abate adiantamentos em aberto (FIFO) até o total bruto
        BigDecimal abatido = adiantamentoService.abater(tatuadorId, totalBruto);
        BigDecimal totalLiquido = totalBruto.subtract(abatido);

        Arquivo comprovante = null;
        if (comprovanteId != null) {
            comprovante = arquivoRepository.findById(comprovanteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comprovante não encontrado com ID: " + comprovanteId));
        }

        PagamentoComissao pagamento = PagamentoComissao.builder()
                .tatuador(tatuador)
                .totalBruto(totalBruto)
                .totalAdiantamentosAbatidos(abatido)
                .totalLiquido(totalLiquido)
                .formaPagamento(formaPagamento)
                .comprovante(comprovante)
                .status(PagamentoComissao.Status.AGENDADO)
                .build();

        for (TransacaoFinanceira t : transacoes) {
            t.setStatusRepasse(StatusRepasse.AGENDADO);
            transacaoRepository.save(t);
            pagamento.getItens().add(PagamentoComissaoItem.builder()
                    .pagamento(pagamento)
                    .transacao(t)
                    .valorRepasse(t.getValorRepasse())
                    .build());
        }

        return PagamentoComissaoDTO.fromEntity(pagamentoRepository.save(pagamento));
    }

    @Transactional
    public PagamentoComissaoDTO confirmar(Long id) {
        PagamentoComissao p = buscar(id);
        if (p.getStatus() == PagamentoComissao.Status.PAGO) {
            throw new ConflictException("Este pagamento já foi confirmado.");
        }
        p.setStatus(PagamentoComissao.Status.PAGO);
        p.setPagoEm(LocalDateTime.now());
        for (PagamentoComissaoItem item : p.getItens()) {
            TransacaoFinanceira t = item.getTransacao();
            t.setStatusRepasse(StatusRepasse.PAGO);
            transacaoRepository.save(t);
        }
        return PagamentoComissaoDTO.fromEntity(pagamentoRepository.save(p));
    }

    @Transactional(readOnly = true)
    public Page<PagamentoComissaoDTO> historico(Long tatuadorId, PagamentoComissao.Status status, Pageable pageable) {
        Page<PagamentoComissao> page = status != null
                ? pagamentoRepository.findByTatuadorIdAndStatusOrderByCreatedAtDesc(tatuadorId, status, pageable)
                : pagamentoRepository.findByTatuadorIdOrderByCreatedAtDesc(tatuadorId, pageable);
        return page.map(PagamentoComissaoDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public PagamentoComissao buscar(Long id) {
        PagamentoComissao p = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento de comissão não encontrado com ID: " + id));
        // força o carregamento dos itens e das transações dentro da transação
        // (PDF/render fora de tx estouraria LazyInitializationException com open-in-view off)
        p.getItens().forEach(i -> i.getTransacao().getDescricao());
        return p;
    }

    /** Recibo dentro de transação read-only: itens/transações carregados. */
    @Transactional(readOnly = true)
    public byte[] gerarReciboPdf(Long id, com.tattos.model.Usuario logado) {
        PagamentoComissao p = buscar(id);
        if (logado != null && logado.isTatuador()
                && !p.getTatuador().getId().equals(logado.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Este recibo pertence a outro tatuador.");
        }
        return reciboPdfService.gerarReciboPagamento(p);
    }
}
