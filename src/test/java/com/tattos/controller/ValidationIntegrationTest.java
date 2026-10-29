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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para validação de Bean Validation nos controllers.
 * Verifica que campos inválidos retornam 400 com o mapa de erros em $.fieldErrors.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.tattos.repository.UsuarioRepository usuarioRepository;

    private Long seedCliente() {
        return usuarioRepository.findByEmail("valid.projeto@tattos.com")
                .map(com.tattos.model.Usuario::getId)
                .orElseGet(() -> usuarioRepository.save(com.tattos.model.Cliente.builder()
                        .nome("Cli Validação").email("valid.projeto@tattos.com").senha("h").ativo(true).build()).getId());
    }

    // ─── Orçamento ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/orcamentos com nome em branco deve retornar 400 com fieldErrors.nome")
    @WithMockUser(roles = "GESTOR")
    void postOrcamentoComNomeBrancoRetorna400() throws Exception {
        String payload = """
                {"nome": "", "valor": 500.00}
                """;

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists());
    }

    @Test
    @DisplayName("POST /api/orcamentos sem valor deve retornar 400 com fieldErrors.valor")
    @WithMockUser(roles = "GESTOR")
    void postOrcamentoSemValorRetorna400() throws Exception {
        String payload = """
                {"nome": "Orçamento Válido"}
                """;

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.valor").exists());
    }

    @Test
    @DisplayName("POST /api/orcamentos com valor zero deve retornar 400 com fieldErrors.valor")
    @WithMockUser(roles = "GESTOR")
    void postOrcamentoComValorZeroRetorna400() throws Exception {
        String payload = """
                {"nome": "Orçamento Inválido", "valor": 0.00}
                """;

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.valor").exists());
    }

    @Test
    @DisplayName("POST /api/orcamentos com dados válidos deve retornar 201")
    @WithMockUser(roles = "GESTOR")
    void postOrcamentoValido() throws Exception {
        String payload = """
                {"nome": "Orçamento Tatuagem Realismo", "valor": 1200.00}
                """;

        mockMvc.perform(post("/api/orcamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Orçamento Tatuagem Realismo"));
    }

    // ─── Projeto ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/projetos com nome em branco deve retornar 400 com fieldErrors.nome")
    @WithMockUser(roles = "GESTOR")
    void postProjetoComNomeBrancoRetorna400() throws Exception {
        String payload = """
                {"nome": "   "}
                """;

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.nome").exists());
    }

    @Test
    @DisplayName("POST /api/projetos com dados válidos deve retornar 201")
    @WithMockUser(roles = "GESTOR")
    void postProjetoValido() throws Exception {
        Long clienteId = seedCliente();
        String payload = """
                {"nome": "Projeto Capa de Costas", "descricao": "Arte japonesa tradicional", "clienteId": %d}
                """.formatted(clienteId);

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Projeto Capa de Costas"));
    }

    // ─── KanbanCard ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/kanban-cards com ordem nula deve retornar 400 com fieldErrors.ordem")
    @WithMockUser(roles = "GESTOR")
    void postKanbanCardSemOrdemRetorna400() throws Exception {
        String payload = """
                {"titulo": "Card sem ordem", "status": "A_FAZER"}
                """;

        mockMvc.perform(post("/api/kanban-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.ordem").exists());
    }

    @Test
    @DisplayName("POST /api/kanban-cards com título em branco deve retornar 400 com fieldErrors.titulo")
    @WithMockUser(roles = "GESTOR")
    void postKanbanCardComTituloBrancoRetorna400() throws Exception {
        String payload = """
                {"titulo": "", "status": "A_FAZER", "ordem": 1}
                """;

        mockMvc.perform(post("/api/kanban-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists());
    }

    @Test
    @DisplayName("POST /api/kanban-cards sem autenticação deve retornar 401")
    void postKanbanCardSemAutenticacaoRetorna401() throws Exception {
        String payload = """
                {"titulo": "Card", "status": "A_FAZER", "ordem": 1}
                """;

        mockMvc.perform(post("/api/kanban-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }
}
