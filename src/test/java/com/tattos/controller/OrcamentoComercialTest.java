package com.tattos.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrcamentoComercialTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("FECHADO sem valorTotal retorna 400")
    @WithMockUser(roles = "GESTOR")
    void fechadoSemValorTotalRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Dragão costas\", \"valor\": 1300.00, \"tipoCobranca\": \"FECHADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POR_SESSAO sem valorSessao retorna 400")
    @WithMockUser(roles = "GESTOR")
    void porSessaoSemValorSessaoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Dragão costas\", \"valor\": 1000.00, \"tipoCobranca\": \"POR_SESSAO\", \"sessoesMin\": 2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("sessoesMax menor que sessoesMin retorna 400")
    @WithMockUser(roles = "GESTOR")
    void sessoesMaxMenorQueMinRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Dragão\", \"valor\": 1000.00, \"tipoCobranca\": \"POR_SESSAO\", \"valorSessao\": 500.00, \"sessoesMin\": 3, \"sessoesMax\": 2}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Orçamento POR_SESSAO válido retorna 201 com campos comerciais")
    @WithMockUser(roles = "GESTOR")
    void porSessaoValidoRetorna201() throws Exception {
        mockMvc.perform(post("/api/v1/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Dragão costas\", \"valor\": 1000.00, \"tipoCobranca\": \"POR_SESSAO\", \"valorSessao\": 500.00, \"sessoesMin\": 2, \"sessoesMax\": 3, \"observacoes\": \"preto e cinza\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCobranca").value("POR_SESSAO"))
                .andExpect(jsonPath("$.valorSessao").value(500.00))
                .andExpect(jsonPath("$.sessoesMin").value(2))
                .andExpect(jsonPath("$.sessoesMax").value(3))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));
    }

    @Test
    @DisplayName("Payload legado (nome+valor) continua retornando 201")
    @WithMockUser(roles = "GESTOR")
    void payloadLegadoContinuaFuncionando() throws Exception {
        mockMvc.perform(post("/api/v1/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Orçamento simples\", \"valor\": 350.00}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RASCUNHO"));
    }
}
