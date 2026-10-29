package com.tattos.security;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * As rotas /api/v1/** devem herdar as mesmas regras de segurança dos paths legados:
 * públicas continuam públicas e restritas por papel continuam restritas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class V1SecurityMatchersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/v1/auth/login anônimo deve chegar ao controller (401 do handler de negócio, com body estruturado)")
    void loginV1EhPublico() throws Exception {
        // Se o filtro de segurança bloqueasse, o 401 viria sem o ErrorResponseDTO estruturado
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"naoexiste@x.com\", \"senha\": \"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.error").value("Falha de Autenticação"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me anônimo deve ser barrado antes do controller")
    void meV1ExigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me")
                        .header("X-Correlation-ID", "anonymous-auth-me"))
                .andExpect(status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .header().string("X-Correlation-ID", "anonymous-auth-me"));
    }

    @Test
    @DisplayName("GET /api/v1/google-calendar/** anônimo não deve ser barrado pelo filtro de segurança")
    void googleCalendarV1EhPublico() throws Exception {
        // Sem permitAll, o filtro retornaria 401; qualquer outro status prova que passou pela segurança
        mockMvc.perform(get("/api/v1/google-calendar/callback").param("code", "x").param("state", "1"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    if (s == 401 || s == 403) {
                        throw new AssertionError("Rota pública bloqueada pelo filtro: " + s);
                    }
                });
    }

    @Test
    @DisplayName("GET /api/v1/aftercare com role CLIENTE deve retornar 403")
    @WithMockUser(roles = "CLIENTE")
    void aftercareV1ProibidoParaCliente() throws Exception {
        mockMvc.perform(get("/api/v1/aftercare/cliente/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/financeiro/transacoes com role CLIENTE deve retornar 403")
    @WithMockUser(roles = "CLIENTE")
    void financeiroV1ProibidoParaCliente() throws Exception {
        mockMvc.perform(get("/api/v1/financeiro/transacoes"))
                .andExpect(status().isForbidden());
    }
}
