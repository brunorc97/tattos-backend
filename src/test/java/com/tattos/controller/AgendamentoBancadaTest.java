package com.tattos.controller;

import com.tattos.model.Bancada;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.BancadaRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AgendamentoBancadaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private BancadaRepository bancadaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Cliente cliente;
    private Tatuador tatuador;
    private Bancada bancada;

    @BeforeEach
    void setUp() {
        agendamentoRepository.deleteAll();
        bancadaRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Bancada Ag").email("agb.c@tattos.com").senha("h").ativo(true).build());
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Bancada Ag").email("agb.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        bancada = bancadaRepository.save(Bancada.builder().nome("Bancada A").build());
    }

    private String payload(Long bancadaId) {
        return "{\"clienteId\": " + cliente.getId()
                + ", \"tatuadorId\": " + tatuador.getId()
                + ", \"dataHora\": \"" + LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0) + "\""
                + ", \"duracaoMinutos\": 60, \"cabine\": \"Bancada A\", \"valor\": 500.00"
                + (bancadaId != null ? ", \"bancadaId\": " + bancadaId : "") + "}";
    }

    @Test
    @DisplayName("Criar com bancadaId persiste e responde com bancadaId/bancadaNome")
    @WithMockUser(roles = "GESTOR")
    void criarComBancadaId() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(bancada.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bancadaId").value(bancada.getId()))
                .andExpect(jsonPath("$.bancadaNome").value("Bancada A"))
                .andExpect(jsonPath("$.cabine").value("Bancada A"));
    }

    @Test
    @DisplayName("Criar com bancadaId inexistente retorna 400")
    @WithMockUser(roles = "GESTOR")
    void criarComBancadaInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(999999L)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar com bancada inativa retorna 400")
    @WithMockUser(roles = "GESTOR")
    void criarComBancadaInativa() throws Exception {
        Bancada inativa = bancadaRepository.save(Bancada.builder().nome("Bancada Z").ativa(false).build());
        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(inativa.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Criar sem bancadaId (legado, so cabine) continua funcionando")
    @WithMockUser(roles = "GESTOR")
    void criarSemBancadaIdLegado() throws Exception {
        mockMvc.perform(post("/api/v1/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload(null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cabine").value("Bancada A"));
    }
}
