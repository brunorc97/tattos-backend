package com.tattos.event.listener;

import com.tattos.event.AgendamentoRealizadoEvent;
import com.tattos.event.SinalPagoEvent;
import com.tattos.model.Agendamento;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gera as transações financeiras automáticas do ciclo de vida do agendamento
 * (sinal pago e saldo do procedimento realizado) desacopladas do AgendamentoService.
 * Roda após o commit da transação de origem: rollback não gera lançamento órfão.
 */
@Component
@RequiredArgsConstructor
public class TransacaoAutomaticaListener {

    private final AgendamentoRepository agendamentoRepository;
    private final TransacaoFinanceiraRepository transacaoRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSinalPago(SinalPagoEvent event) {
        Agendamento agendamento = agendamentoRepository.findById(event.agendamentoId()).orElse(null);
        if (agendamento == null
                || agendamento.getValorSinal() == null
                || agendamento.getValorSinal().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        TransacaoFinanceira transacaoSinal = TransacaoFinanceira.builder()
                .descricao("Sinal Recebido (PIX) - Agendamento #" + agendamento.getId()
                        + " (" + agendamento.getCliente().getNome() + ")")
                .valor(agendamento.getValorSinal())
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuador(agendamento.getTatuador())
                .agendamento(agendamento)
                .categoria("Sinal Reserva")
                .formaPagamento("PIX")
                .build();
        transacaoSinal.calcularRepasse();
        transacaoRepository.save(transacaoSinal);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAgendamentoRealizado(AgendamentoRealizadoEvent event) {
        Agendamento agendamento = agendamentoRepository.findById(event.agendamentoId()).orElse(null);
        if (agendamento == null || agendamento.getValor() == null) {
            return;
        }

        BigDecimal desconto = agendamento.getDescontoAplicado() != null
                ? agendamento.getDescontoAplicado() : BigDecimal.ZERO;
        BigDecimal valorSaldoRestante = agendamento.getValor().subtract(desconto);
        if (agendamento.isSinalPago() && agendamento.getValorSinal() != null) {
            valorSaldoRestante = valorSaldoRestante.subtract(agendamento.getValorSinal());
        }
        if (valorSaldoRestante.compareTo(BigDecimal.ZERO) < 0) {
            valorSaldoRestante = BigDecimal.ZERO;
        }

        TransacaoFinanceira transacao = TransacaoFinanceira.builder()
                .descricao("Procedimento Concluído (Saldo) - Agendamento #" + agendamento.getId()
                        + " (" + agendamento.getCliente().getNome() + ")")
                .valor(valorSaldoRestante)
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuador(agendamento.getTatuador())
                .agendamento(agendamento)
                .categoria("Procedimento Tattoo")
                .formaPagamento("A Definir")
                .build();
        transacao.calcularRepasse();
        transacaoRepository.save(transacao);
    }
}
