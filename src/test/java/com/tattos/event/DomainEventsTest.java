package com.tattos.event;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import com.tattos.service.AgendamentoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Sem @Transactional: os listeners são @TransactionalEventListener(AFTER_COMMIT)
 * e só disparam após commit real.
 */
@SpringBootTest
@ActiveProfiles("test")
class DomainEventsTest {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;

    private Agendamento agendamento;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Cliente cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Carlos").email("carlos@eventos.com").senha("hash").ativo(true).build());
        Tatuador tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Rodrigo").email("rodrigo@eventos.com").senha("hash")
                .comissaoPercentual(new BigDecimal("60.00")).ativo(true).build());

        agendamento = agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador)
                .dataHora(LocalDateTime.now().plusDays(3))
                .cabine("B1")
                .valor(new BigDecimal("1000.00"))
                .valorSinal(new BigDecimal("300.00"))
                .status(StatusAgendamento.AGENDADO)
                .build());
    }

    @AfterEach
    void tearDown() {
        transacaoRepository.deleteAll();
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Confirmar sinal deve gerar transação 'Sinal Reserva' via evento após commit")
    void sinalPagoGeraTransacaoAposCommit() {
        agendamentoService.confirmarPagamentoSinal(agendamento.getId());

        assertThat(transacaoRepository.findAll())
                .anySatisfy(t -> {
                    assertThat(t.getCategoria()).isEqualTo("Sinal Reserva");
                    assertThat(t.getValor()).isEqualByComparingTo("300.00");
                });
    }

    @Test
    @DisplayName("Marcar como REALIZADO deve gerar transação 'Procedimento Tattoo' com saldo restante")
    void realizadoGeraTransacaoDeSaldo() {
        agendamentoService.confirmarPagamentoSinal(agendamento.getId());
        agendamentoService.atualizarStatus(agendamento.getId(), StatusAgendamento.REALIZADO, null);

        assertThat(transacaoRepository.findAll())
                .anySatisfy(t -> {
                    assertThat(t.getCategoria()).isEqualTo("Procedimento Tattoo");
                    assertThat(t.getValor()).isEqualByComparingTo("700.00");
                });
    }

    @Test
    @DisplayName("Publicar SinalPagoEvent diretamente deve acionar o listener e gerar transação")
    void listenerReageAoEventoDiretamente() {
        Long id = agendamento.getId();

        transactionTemplate.executeWithoutResult(status ->
                applicationEventPublisher.publishEvent(new SinalPagoEvent(id)));

        assertThat(transacaoRepository.findAll())
                .anySatisfy(t -> assertThat(t.getCategoria()).isEqualTo("Sinal Reserva"));
    }

    @Test
    @DisplayName("Rollback após confirmação de sinal NÃO deve gerar transação financeira")
    void rollbackNaoGeraTransacao() {
        Long id = agendamento.getId();

        assertThatThrownBy(() -> transactionTemplate.execute(status -> {
            agendamentoService.confirmarPagamentoSinal(id);
            throw new RuntimeException("falha forçada para rollback");
        })).isInstanceOf(RuntimeException.class);

        assertThat(transacaoRepository.findAll()).isEmpty();
        assertThat(agendamentoRepository.findById(id)).get()
                .extracting(Agendamento::isSinalPago).isEqualTo(false);
    }
}
