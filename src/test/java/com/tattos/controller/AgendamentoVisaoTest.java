package com.tattos.controller;

import com.tattos.model.Agendamento;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAgendamento;
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
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AgendamentoVisaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Cliente cliente;
    private Tatuador tatA;
    private Tatuador tatB;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Visao").email("cliv@tattos.com").senha("h").ativo(true).build());
        tatA = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat A").email("tata.v@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        tatB = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat B").email("tatb.v@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatA).cabine("Cabine 01")
                .dataHora(LocalDateTime.now().plusDays(2)).valor(new BigDecimal("500")).build());
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatB).cabine("Cabine 02")
                .dataHora(LocalDateTime.now().plusDays(3)).valor(new BigDecimal("500")).build());
        agendamentoRepository.save(Agendamento.builder()
                .cliente(cliente).tatuador(tatA).cabine("Cabine 01")
                .dataHora(LocalDateTime.now().minusDays(5)).valor(new BigDecimal("300"))
                .status(StatusAgendamento.REALIZADO).build());
    }

    @Test
    @DisplayName("Tatuador com tatuadorId alheio recebe apenas a própria agenda")
    void tatuadorEscopoProprio() throws Exception {
        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(tatA);

        mockMvc.perform(get("/api/v1/agendamentos")
                        .header("Authorization", token)
                        .param("tatuadorId", tatB.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].tatuadorId").value(tatA.getId()));
    }

    @Test
    @DisplayName("Gestor filtra por cabine")
    @WithMockUser(roles = "GESTOR")
    void gestorFiltraPorCabine() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos").param("cabine", "Cabine 02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].cabine").value("Cabine 02"));
    }

    @Test
    @DisplayName("futuras=true exclui agendamentos passados")
    @WithMockUser(roles = "GESTOR")
    void apenasFuturas() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos").param("futuras", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("Cliente vê apenas as próprias sessões")
    void clienteVeApenasProprias() throws Exception {
        Cliente outro = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Outro Cli").email("outro.v@tattos.com").senha("h").ativo(true).build());
        agendamentoRepository.save(Agendamento.builder()
                .cliente(outro).tatuador(tatA).cabine("Cabine 01")
                .dataHora(LocalDateTime.now().plusDays(1)).valor(new BigDecimal("400")).build());

        String token = "Bearer " + jwtTokenProvider.generateTokenForUser(cliente);
        mockMvc.perform(get("/api/v1/agendamentos").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
