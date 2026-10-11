package com.tattos.service;

import com.tattos.model.*;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.InsumoRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class NotificacaoSchedulerServiceTest {

    @Autowired
    private NotificacaoSchedulerService schedulerService;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;
    private Tatuador tatuador;
    private Gestor gestor;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        insumoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = Cliente.builder()
                .nome("Cliente Teste")
                .email("cliente.lembrete@tattos.com")
                .senha("pass123")
                .ativo(true)
                .build();
        cliente = (Cliente) usuarioRepository.save(cliente);

        tatuador = Tatuador.builder()
                .nome("Tatuador Teste")
                .email("tatuador.lembrete@tattos.com")
                .senha("pass123")
                .especialidade("Old School")
                .ativo(true)
                .build();
        tatuador = (Tatuador) usuarioRepository.save(tatuador);

        gestor = Gestor.builder()
                .nome("Gestor Geral")
                .email("gestor.lembrete@tattos.com")
                .senha("pass123")
                .cargo("Gerente")
                .ativo(true)
                .build();
        gestor = (Gestor) usuarioRepository.save(gestor);
    }

    @Test
    @DisplayName("Deve enviar lembrete para agendamentos nas próximas 24h e garantir idempotência")
    void deveProcessarLembretesComIdempotencia() {
        LocalDateTime agora = LocalDateTime.now();

        // Agendamento 1: Daqui a 10h (dentro da janela de 24h) - Deve ser notificado
        Agendamento ag1 = Agendamento.builder()
                .cliente(cliente)
                .tatuador(tatuador)
                .dataHora(agora.plusHours(10))
                .duracaoMinutos(60)
                .dataHoraFim(agora.plusHours(11))
                .cabine("Cabine 01")
                .status(StatusAgendamento.AGENDADO)
                .valor(new BigDecimal("300.00"))
                .lembreteEnviado(false)
                .build();
        agendamentoRepository.save(ag1);

        // Agendamento 2: Daqui a 30h (fora da janela de 24h) - Não deve ser notificado
        Agendamento ag2 = Agendamento.builder()
                .cliente(cliente)
                .tatuador(tatuador)
                .dataHora(agora.plusHours(30))
                .duracaoMinutos(60)
                .dataHoraFim(agora.plusHours(31))
                .cabine("Cabine 02")
                .status(StatusAgendamento.AGENDADO)
                .valor(new BigDecimal("400.00"))
                .lembreteEnviado(false)
                .build();
        agendamentoRepository.save(ag2);

        // Agendamento 3: Daqui a 5h porém CANCELADO - Não deve ser notificado
        Agendamento ag3 = Agendamento.builder()
                .cliente(cliente)
                .tatuador(tatuador)
                .dataHora(agora.plusHours(5))
                .duracaoMinutos(60)
                .dataHoraFim(agora.plusHours(6))
                .cabine("Cabine 03")
                .status(StatusAgendamento.CANCELADO)
                .valor(new BigDecimal("250.00"))
                .lembreteEnviado(false)
                .build();
        agendamentoRepository.save(ag3);

        // 1ª execução: deve processar exatamente 1 agendamento
        int enviados = schedulerService.processarLembretesAgendamentos();
        assertEquals(1, enviados);

        Agendamento atualizado = agendamentoRepository.findById(ag1.getId()).orElseThrow();
        assertTrue(atualizado.isLembreteEnviado());

        // 2ª execução (teste de idempotência): não deve reprocessar ag1
        int reprocessados = schedulerService.processarLembretesAgendamentos();
        assertEquals(0, reprocessados);
    }

    @Test
    @DisplayName("Deve identificar e emitir alertas para insumos com estoque crítico")
    void deveProcessarAlertasEstoqueCritico() {
        // Insumo crítico
        Insumo tintaPreta = Insumo.builder()
                .codigo("TINTA-BLK")
                .nome("Tinta Preta 30ml")
                .quantidadeAtual(3)
                .nivelCritico(10)
                .unidadeMedida("UN")
                .categoria("Pigmentos")
                .build();
        insumoRepository.save(tintaPreta);

        // Insumo saudável
        Insumo luvaNitrilica = Insumo.builder()
                .codigo("LUVA-NIT-M")
                .nome("Luva Nitrílica M")
                .quantidadeAtual(50)
                .nivelCritico(10)
                .unidadeMedida("CX")
                .categoria("EPIs")
                .build();
        insumoRepository.save(luvaNitrilica);

        int alertados = schedulerService.processarAlertasEstoqueCritico();
        assertEquals(1, alertados);
    }
}
