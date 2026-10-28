package com.tattos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CorrelationIdFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Requisição autenticada sem header deve receber X-Correlation-ID gerado na resposta")
    @WithMockUser(roles = "GESTOR")
    void geraCorrelationIdQuandoAusente() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/insumos"))
                .andExpect(status().isOk())
                .andReturn();

        String correlationId = result.getResponse().getHeader("X-Correlation-ID");
        assertThat(correlationId).isNotBlank();
        assertThat(UUID.fromString(correlationId)).isNotNull();
    }

    @Test
    @DisplayName("Requisição com X-Correlation-ID deve ter o mesmo valor ecoado na resposta")
    @WithMockUser(roles = "GESTOR")
    void ecoaCorrelationIdRecebido() throws Exception {
        mockMvc.perform(get("/api/insumos").header("X-Correlation-ID", "abc-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", "abc-123"));
    }

    @Test
    @DisplayName("Endpoint público sem token não deve quebrar e deve receber correlation id")
    void endpointPublicoSemUsuario() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"));
    }
}
