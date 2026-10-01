package com.tattos.service;

import com.tattos.dto.request.TransacaoRequestDTO;
import com.tattos.dto.response.BalancoFinanceiroDTO;
import com.tattos.dto.response.TransacaoResponseDTO;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Agendamento;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoFinanceiraRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public TransacaoResponseDTO criar(TransacaoRequestDTO dto) {
        Usuario tatuador = null;
        if (dto.getTatuadorId() != null) {
            tatuador = usuarioRepository.findById(dto.getTatuadorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com ID: " + dto.getTatuadorId()));
        }

        Agendamento agendamento = null;
        if (dto.getAgendamentoId() != null) {
            agendamento = agendamentoRepository.findById(dto.getAgendamentoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + dto.getAgendamentoId()));
        }

        BigDecimal comissao = dto.getComissaoPercentual();
        BigDecimal valorRepasse = BigDecimal.ZERO;

        if (TipoTransacao.RECEITA.equals(dto.getTipo()) && tatuador != null) {
            if (comissao == null || comissao.compareTo(BigDecimal.ZERO) <= 0) {
                comissao = tatuador.getComissaoPercentual() != null ? tatuador.getComissaoPercentual() : new BigDecimal("70.00");
            }
            valorRepasse = dto.getValor().multiply(comissao).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else {
            comissao = BigDecimal.ZERO;
        }

        TransacaoFinanceira transacao = TransacaoFinanceira.builder()
                .descricao(dto.getDescricao().trim())
                .valor(dto.getValor())
                .tipo(dto.getTipo())
                .data(dto.getData())
                .status(dto.getStatus() != null ? dto.getStatus() : StatusTransacao.CONCLUIDO)
                .tatuador(tatuador)
                .categoria(dto.getCategoria())
                .formaPagamento(dto.getFormaPagamento())
                .comissaoPercentual(comissao)
                .valorRepasse(valorRepasse)
                .agendamento(agendamento)
                .build();

        TransacaoFinanceira salva = transacaoRepository.save(transacao);
        return TransacaoResponseDTO.fromEntity(salva);
    }

    /**
     * Calcula o total de repasses de comissão devidos ao tatuador no intervalo informado.
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularRepasseTatuador(Long tatuadorId, LocalDate inicio, LocalDate fim) {
        if (!usuarioRepository.existsById(tatuadorId)) {
            throw new ResourceNotFoundException("Tatuador não encontrado com ID: " + tatuadorId);
        }
        LocalDate dataInicio = inicio != null ? inicio : LocalDate.now().withDayOfMonth(1);
        LocalDate dataFim = fim != null ? fim : YearMonth.from(dataInicio).atEndOfMonth();

        return transacaoRepository.sumRepassesByTatuadorAndPeriodo(tatuadorId, dataInicio, dataFim);
    }

    /**
     * Balanço Gerencial "Smile Now, Cry Later":
     * - Faturamento Bruto (Smile Now)
     * - Despesas + Repasses (Cry Later)
     * - Lucro Líquido e Margem
     */
    @Transactional(readOnly = true)
    public BalancoFinanceiroDTO obterBalancoMensal(int mes, int ano) {
        YearMonth ym = YearMonth.of(ano, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fim = ym.atEndOfMonth();

        BigDecimal faturamentoBruto = transacaoRepository.sumReceitasByPeriodo(inicio, fim);
        BigDecimal totalDespesas = transacaoRepository.sumDespesasByPeriodo(inicio, fim);
        BigDecimal totalRepasses = transacaoRepository.sumTotalRepassesByPeriodo(inicio, fim);

        BigDecimal totalSaidas = totalDespesas.add(totalRepasses);
        BigDecimal lucroLiquido = faturamentoBruto.subtract(totalSaidas);

        BigDecimal margemLucro = BigDecimal.ZERO;
        if (faturamentoBruto.compareTo(BigDecimal.ZERO) > 0) {
            margemLucro = lucroLiquido.multiply(new BigDecimal("100")).divide(faturamentoBruto, 2, RoundingMode.HALF_UP);
        }

        List<TransacaoFinanceira> transacoesPeriodo = transacaoRepository.findByDataBetween(inicio, fim);

        return BalancoFinanceiroDTO.builder()
                .mes(mes)
                .ano(ano)
                .faturamentoBruto(faturamentoBruto)
                .totalDespesas(totalDespesas)
                .totalRepassesTatuadores(totalRepasses)
                .totalSaidas(totalSaidas)
                .lucroLiquido(lucroLiquido)
                .margemLucroPercentual(margemLucro)
                .totalTransacoes(transacoesPeriodo.size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponseDTO> listar(TipoTransacao tipo, Long tatuadorId, StatusTransacao status, LocalDate inicio, LocalDate fim) {
        return transacaoRepository.findComFiltros(tipo, tatuadorId, status, inicio, fim).stream()
                .map(TransacaoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponseDTO buscarPorId(Long id) {
        TransacaoFinanceira t = transacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação financeira não encontrada com ID: " + id));
        return TransacaoResponseDTO.fromEntity(t);
    }

    @Transactional
    public void deletar(Long id) {
        if (!transacaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transação financeira não encontrada com ID: " + id);
        }
        transacaoRepository.deleteById(id);
    }
}
