package com.tattos.controller;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.UsuarioRepository;
import com.tattos.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AgendaVisaoPeriodoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Tatuador tatA;
    private Tatuador tatB;
    private LocalDate base;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        Cliente cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Agenda").email("cliag@tattos.com").senha("h").ativo(true).build());
        tatA = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat A").email("tata.ag@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        tatB = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat B").email("tatb.ag@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        base = LocalDate.now().plusDays(7);
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatA).cabine("B1")
                .dataHora(base.atTime(10, 0)).valor(new BigDecimal("500")).build());
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatA).cabine("B1")
                .dataHora(base.atTime(14, 0)).valor(new BigDecimal("500")).build());
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatB).cabine("B2")
                .dataHora(base.plusDays(1).atTime(11, 0)).valor(new BigDecimal("500")).build());
        // fora do período
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatA).cabine("B1")
                .dataHora(base.plusDays(10).atTime(10, 0)).valor(new BigDecimal("500")).build());
    }

    @Test
    @DisplayName("Agenda retorna somente dentro do período, ordenado por dataHora")
    @WithMockUser(roles = "GESTOR")
    void agendaRetornaSomenteDentroDoPeriodo() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos/agenda")
                        .param("inicio", base.toString())
                        .param("fim", base.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].dataHora").value(base.atTime(10, 0).withSecond(0).toString() + ":00"));
    }

    @Test
    @DisplayName("Agenda com inicio maior que fim retorna 400")
    @WithMockUser(roles = "GESTOR")
    void agendaComInicioMaiorQueFimRetorna400() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos/agenda")
                        .param("inicio", base.plusDays(1).toString())
                        .param("fim", base.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tatuador vê somente a própria agenda no período")
    void tatuadorVeSomentePropriaAgenda() throws Exception {
        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatA);
        mockMvc.perform(get("/api/v1/agendamentos/agenda")
                        .header("Authorization", token)
                        .param("inicio", base.toString())
                        .param("fim", base.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tatuadorId").value(tatA.getId()));
    }
}
