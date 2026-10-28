package com.tattos.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante o versionamento /api/v1 mantendo os aliases legados funcionando.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApiVersioningTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/agendamentos deve responder 200")
    @WithMockUser(roles = "GESTOR")
    void v1Agendamentos() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/insumos deve responder 200")
    @WithMockUser(roles = "GESTOR")
    void v1Insumos() throws Exception {
        mockMvc.perform(get("/api/v1/insumos"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/financeiro/transacoes deve responder 200")
    @WithMockUser(roles = "GESTOR")
    void v1Transacoes() throws Exception {
        mockMvc.perform(get("/api/v1/financeiro/transacoes"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/projetos e /api/v1/orcamentos devem responder 200")
    @WithMockUser(roles = "GESTOR")
    void v1ProjetosEOrcamentos() throws Exception {
        mockMvc.perform(get("/api/v1/projetos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/orcamentos")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aliases legados sem versão continuam funcionando")
    @WithMockUser(roles = "GESTOR")
    void aliasesLegados() throws Exception {
        mockMvc.perform(get("/api/agendamentos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/insumos")).andExpect(status().isOk());
        mockMvc.perform(get("/api/financeiro/transacoes")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rota v1 sem autenticação deve retornar 401")
    void v1SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/agendamentos"))
                .andExpect(status().isUnauthorized());
    }
}
