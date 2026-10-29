package com.tattos.service;

import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Arquivo;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.Usuario;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.ArquivoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Adiantamentos a tatuadores: transações do tipo ADIANTAMENTO com controle de
 * abatimento (FIFO) nos pagamentos de comissão.
 */
@Service
@RequiredArgsConstructor
public class AdiantamentoService {

    private final TransacaoFinanceiraRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArquivoRepository arquivoRepository;

    @Transactional
    public TransacaoResponseDTO criar(Long tatuadorId, BigDecimal valor, Long reciboId, String observacao) {
        Usuario tatuador = usuarioRepository.findById(tatuadorId)
                .filter(Usuario::isTatuador)
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + tatuadorId));

        Arquivo recibo = null;
        if (reciboId != null) {
            recibo = arquivoRepository.findById(reciboId)
                    .orElseThrow(() -> new ResourceNotFoundException("Recibo não encontrado com ID: " + reciboId));
        }

        TransacaoFinanceira t = transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao(observacao != null && !observacao.isBlank()
                        ? "Adiantamento — " + observacao.trim()
                        : "Adiantamento")
                .valor(valor)
                .tipo(TipoTransacao.ADIANTAMENTO)
                .data(LocalDate.now())
                .tatuador(tatuador)
                .categoria("Adiantamento")
                .recibo(recibo)
                .build());
        return TransacaoResponseDTO.fromEntity(t);
    }

    @Transactional(readOnly = true)
    public Page<TransacaoFinanceira> listar(Long tatuadorId, boolean apenasEmAberto, Pageable pageable) {
        if (tatuadorId == null) {
            return transacaoRepository.findComFiltrosPaginado(TipoTransacao.ADIANTAMENTO, null, null, null, null, pageable);
        }
        if (apenasEmAberto) {
            return transacaoRepository.findAdiantamentosEmAberto(tatuadorId, pageable);
        }
        return transacaoRepository.findComFiltrosPaginado(TipoTransacao.ADIANTAMENTO, tatuadorId, null, null, null, pageable);
    }

    /** Σ(valor) − Σ(abatido) dos adiantamentos do tatuador. */
    @Transactional(readOnly = true)
    public BigDecimal saldoEmAberto(Long tatuadorId) {
        List<TransacaoFinanceira> abertos = transacaoRepository
                .findAdiantamentosEmAbertoList(tatuadorId);
        return abertos.stream()
                .map(t -> t.getValor().subtract(t.getAbatido()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Abate adiantamentos em aberto (mais antigos primeiro) até o limite.
     * Retorna o total abatido. Roda dentro da transação do pagamento.
     */
    @Transactional
    public BigDecimal abater(Long tatuadorId, BigDecimal limite) {
        BigDecimal restante = limite;
        BigDecimal abatidoTotal = BigDecimal.ZERO;
        for (TransacaoFinanceira ad : transacaoRepository.findAdiantamentosEmAbertoParaAbater(tatuadorId)) {
            if (restante.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal saldoAd = ad.getValor().subtract(ad.getAbatido());
            BigDecimal abater = saldoAd.min(restante);
            ad.setAbatido(ad.getAbatido().add(abater));
            transacaoRepository.save(ad);
            restante = restante.subtract(abater);
            abatidoTotal = abatidoTotal.add(abater);
        }
        return abatidoTotal;
    }
}
