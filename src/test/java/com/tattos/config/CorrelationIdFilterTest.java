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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsStringIgnoringCase;

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
        mockMvc.perform(get("/api/v1/google-calendar/callback")
                        .param("code", "oauth-code")
                        .param("state", "opaque-state"))
                .andExpect(status().isFound())
                .andExpect(header().exists("X-Correlation-ID"));
    }

    @Test
    @DisplayName("Preflight do frontend permite X-Correlation-ID")
    void preflightPermiteCorrelationId() throws Exception {
        mockMvc.perform(options("/api/v1/agendamentos")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type,x-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsStringIgnoringCase("X-Correlation-ID")));
    }

    @Test
    @DisplayName("Preflight aceita a origem loopback IPv4 usada pelo navegador local")
    void preflightPermiteOrigemLoopbackIpv4() throws Exception {
        mockMvc.perform(options("/api/v1/agendamentos")
                        .header("Origin", "http://127.0.0.1:3000")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type,x-correlation-id"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:3000"))
                .andExpect(header().string("Access-Control-Allow-Headers", containsStringIgnoringCase("X-Correlation-ID")));
    }

    @Test
    @DisplayName("Frontend pode ler o correlation ID ecoado na resposta")
    @WithMockUser(roles = "GESTOR")
    void respostaExpõeCorrelationIdParaOrigemDoFrontend() throws Exception {
        mockMvc.perform(get("/api/v1/insumos")
                        .header("Origin", "http://localhost:3000")
                        .header("X-Correlation-ID", "browser-trace-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", "browser-trace-1"))
                .andExpect(header().string("Access-Control-Expose-Headers", containsStringIgnoringCase("X-Correlation-ID")));
    }
}
