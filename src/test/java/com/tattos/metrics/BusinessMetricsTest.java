package com.tattos.metrics;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAgendamento;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

/**
 * Verifica a exposição das métricas de negócio no endpoint /actuator/prometheus.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BusinessMetricsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Cliente cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Carlos").email("carlos@metrics.com").senha("hash").ativo(true).build());
        Tatuador tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Rodrigo").email("rodrigo@metrics.com").senha("hash").ativo(true).build());

        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador)
                .dataHora(LocalDateTime.now().plusDays(1))
                .cabine("A1")
                .valor(new BigDecimal("1000.00"))
                .valorSinal(new BigDecimal("300.00"))
                .sinalPago(true)
                .status(StatusAgendamento.CONFIRMADO)
                .build());

        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatuador)
                .dataHora(LocalDateTime.now().plusDays(2))
                .cabine("A2")
                .valor(new BigDecimal("500.00"))
                .valorSinal(new BigDecimal("150.00"))
                .status(StatusAgendamento.AGENDADO)
                .build());
    }

    @Test
    @DisplayName("/actuator/prometheus deve expor métricas de agendamentos por status")
    void expoeAgendamentosPorStatus() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tattos_agendamentos_status")))
                .andExpect(content().string(containsString("status=\"CONFIRMADO\"")));
    }

    @Test
    @DisplayName("/actuator/prometheus deve expor taxa de sinais pagos e faturamento diário")
    void expoeTaxaSinaisEFaturamento() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("tattos_sinais_taxa_pagamento")))
                .andExpect(content().string(containsString("tattos_faturamento_diario")));
    }
}
