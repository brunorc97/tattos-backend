package com.tattos.controller;

import com.tattos.repository.OrcamentoRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrcamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @BeforeEach
    void setUp() {
        orcamentoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/orcamentos deve retornar 200 com lista")
    @WithMockUser(roles = "GESTOR")
    void listar() throws Exception {
        mockMvc.perform(get("/api/orcamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/orcamentos válido deve retornar 201")
    @WithMockUser(roles = "GESTOR")
    void criarValido() throws Exception {
        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Orçamento Realismo\", \"valor\": 1200.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Orçamento Realismo"));
    }

    @Test
    @DisplayName("POST /api/orcamentos inválido deve retornar 400 com fieldErrors")
    @WithMockUser(roles = "GESTOR")
    void criarInvalido() throws Exception {
        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\", \"valor\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists())
                .andExpect(jsonPath("$.fieldErrors.valor").exists());
    }
}
