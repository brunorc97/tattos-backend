package com.tattos.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que os schemas dos DTOs publicados em /v3/api-docs trazem
 * exemplos de payload para o time de Front-end.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiExamplesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Schemas dos principais DTOs devem conter exemplos")
    void schemasTemExemplos() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.AgendamentoRequestDTO.properties.dataHora.example").exists())
                .andExpect(jsonPath("$.components.schemas.InsumoRequestDTO.properties.nome.example").exists())
                .andExpect(jsonPath("$.components.schemas.TransacaoRequestDTO.properties.valor.example").exists())
                .andExpect(jsonPath("$.components.schemas.AgendamentoResponseDTO.properties.id.example").exists());
    }
}
