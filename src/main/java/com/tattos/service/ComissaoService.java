package com.tattos.service;

import com.tattos.dto.response.FechamentoComissaoDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.Usuario;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Fechamento de comissões de um tatuador em um período, com o status de
 * repasse (PENDENTE/AGENDADO/PAGO) exposto por item.
 */
@Service
@RequiredArgsConstructor
public class ComissaoService {

    private final TransacaoFinanceiraRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public FechamentoComissaoDTO fechamento(Long tatuadorId, LocalDate inicio, LocalDate fim, Usuario logado) {
        if (inicio == null || fim == null || inicio.isAfter(fim)) {
            throw new BusinessException("Período inválido: 'inicio' e 'fim' são obrigatórios e inicio deve ser menor ou igual a fim.");
        }
        if (logado != null && logado.isTatuador() && !logado.getId().equals(tatuadorId)) {
            throw new AccessDeniedException("Tatuador só pode ver o próprio fechamento.");
        }
        Usuario tatuador = usuarioRepository.findById(tatuadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + tatuadorId));

        List<TransacaoFinanceira> transacoes = transacaoRepository
                .findComFiltros(TipoTransacao.RECEITA, tatuadorId, null, inicio, fim);

        List<FechamentoComissaoDTO.ItemComissaoDTO> itens = transacoes.stream()
                .map(t -> FechamentoComissaoDTO.ItemComissaoDTO.builder()
                        .transacaoId(t.getId())
                        .agendamentoId(t.getAgendamento() != null ? t.getAgendamento().getId() : null)
                        .data(t.getData())
                        .descricao(t.getDescricao())
                        .valorBruto(t.getValor())
                        .comissaoPercentual(t.getComissaoPercentual())
                        .valorRepasse(t.getValorRepasse())
                        .formaPagamento(t.getFormaPagamento())
                        .statusRepasse(t.getStatusRepasse())
                        .build())
                .toList();

        BigDecimal totalBruto = itens.stream()
                .map(i -> i.getValorBruto() != null ? i.getValorBruto() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRepasse = itens.stream()
                .map(i -> i.getValorRepasse() != null ? i.getValorRepasse() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FechamentoComissaoDTO.builder()
                .tatuadorId(tatuador.getId())
                .tatuadorNome(tatuador.getNome())
                .especialidade(tatuador.getEspecialidade())
                .comissaoPercentualPadrao(tatuador.getComissaoPercentual())
                .periodoInicio(inicio)
                .periodoFim(fim)
                .totalAtendimentos(itens.size())
                .totalBruto(totalBruto)
                .totalComissaoRepasse(totalRepasse)
                .totalRetidoEstudio(totalBruto.subtract(totalRepasse))
                .itens(itens)
                .build();
    }
}
