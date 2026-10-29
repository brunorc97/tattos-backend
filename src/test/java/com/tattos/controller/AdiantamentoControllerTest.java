package com.tattos.controller;

import com.tattos.model.Tatuador;
import com.tattos.repository.TransacaoFinanceiraRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdiantamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        usuarioRepository.deleteAll();
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Adiant").email("adiant.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
    }

    @Test
    @DisplayName("Gestor cria adiantamento e consulta saldo em aberto")
    @WithMockUser(roles = "GESTOR")
    void criarESaldo() throws Exception {
        mockMvc.perform(post("/api/v1/financeiro/adiantamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + tatuador.getId() + ", \"valor\": 500.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value("ADIANTAMENTO"));

        mockMvc.perform(post("/api/v1/financeiro/adiantamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + tatuador.getId() + ", \"valor\": 200.00}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/financeiro/adiantamentos")
                        .param("tatuadorId", tatuador.getId().toString())
                        .param("emAberto", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEmAberto").value(700.00))
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Tatuador não cria adiantamento (403)")
    @WithMockUser(roles = "TATUADOR")
    void tatuadorProibido() throws Exception {
        mockMvc.perform(post("/api/v1/financeiro/adiantamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": 1, \"valor\": 100.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Saldo de tatuador sem adiantamentos retorna 0 (sem 500)")
    @WithMockUser(roles = "GESTOR")
    void saldoZeradoSem500() throws Exception {
        mockMvc.perform(get("/api/v1/financeiro/adiantamentos")
                        .param("tatuadorId", tatuador.getId().toString())
                        .param("emAberto", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoEmAberto").value(0.0))
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Test
    @DisplayName("Tatuador inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void tatuadorInexistente() throws Exception {
        mockMvc.perform(post("/api/v1/financeiro/adiantamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": 99999, \"valor\": 100.00}"))
                .andExpect(status().isNotFound());
    }
}
