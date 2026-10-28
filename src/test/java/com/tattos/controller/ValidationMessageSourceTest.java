package com.tattos.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante que as mensagens de erro/validação vêm de messages.properties
 * (MessageSource), e não de strings literais no código.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ValidationMessageSourceTest {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageSource messageSource;

    @Test
    @DisplayName("MessageSource deve resolver as chaves de validação usadas nos DTOs")
    void messageSourceResolveChavesDeValidacao() {
        assertThat(messageSource.getMessage("validation.required", null, PT_BR))
                .isEqualTo("O campo é obrigatório");
        assertThat(messageSource.getMessage("validation.insumo.nome.required", null, PT_BR))
                .isEqualTo("O nome do insumo é obrigatório");
        assertThat(messageSource.getMessage("error.not_found", null, PT_BR))
                .isEqualTo("O recurso solicitado não foi encontrado no sistema.");
    }

    @Test
    @DisplayName("GET insumo inexistente deve responder mensagem centralizada de error.not_found")
    @WithMockUser(roles = "GESTOR")
    void notFoundUsaMensagemCentralizada() throws Exception {
        String esperado = messageSource.getMessage("error.not_found", null, PT_BR);

        mockMvc.perform(get("/api/insumos/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(esperado));
    }

    @Test
    @DisplayName("POST /api/insumos com nome em branco deve responder mensagem vinda do messages.properties")
    @WithMockUser(roles = "GESTOR")
    void validacaoUsaMensagemCentralizada() throws Exception {
        String esperado = messageSource.getMessage("validation.insumo.nome.required", null, PT_BR);

        mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\", \"quantidade\": 10, \"nivelCritico\": 2}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").value(esperado));
    }
}
