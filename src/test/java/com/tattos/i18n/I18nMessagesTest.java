package com.tattos.i18n;

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

/**
 * Mensagens de validação devem seguir o header Accept-Language:
 * en → inglês; ausente/pt-BR → português (padrão).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class I18nMessagesTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String INVALIDO = "{\"nome\": \"\", \"quantidade\": 10, \"nivelCritico\": 2}";

    @Test
    @DisplayName("Accept-Language: en deve retornar mensagem em inglês")
    @WithMockUser(roles = "GESTOR")
    void mensagemEmIngles() throws Exception {
        mockMvc.perform(post("/api/insumos")
                        .header("Accept-Language", "en")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").value("The supply name is required"));
    }

    @Test
    @DisplayName("Sem Accept-Language deve retornar mensagem em português (padrão)")
    @WithMockUser(roles = "GESTOR")
    void mensagemPadraoPortugues() throws Exception {
        mockMvc.perform(post("/api/insumos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").value("O nome do insumo é obrigatório"));
    }
}
