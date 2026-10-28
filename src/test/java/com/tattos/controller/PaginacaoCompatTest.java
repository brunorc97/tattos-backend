package com.tattos.controller;

import com.tattos.model.Insumo;
import com.tattos.repository.AgendamentoRepository;
import com.tattos.repository.InsumoRepository;
import com.tattos.repository.TransacaoFinanceiraRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante o contrato paginado (Page<T>) nas rotas de listagem padrão,
 * incluindo aliases legados /paginado e /paged.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PaginacaoCompatTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @BeforeEach
    void setUp() {
        insumoRepository.deleteAll();
        agendamentoRepository.deleteAll();
        transacaoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/insumos deve retornar Page com content, totalElements e totalPages")
    @WithMockUser(roles = "GESTOR")
    void listarInsumosRetornaPagina() throws Exception {
        for (int i = 1; i <= 25; i++) {
            insumoRepository.save(Insumo.builder()
                    .nome(String.format("Insumo %02d", i))
                    .quantidadeAtual(10)
                    .nivelCritico(2)
                    .build());
        }

        mockMvc.perform(get("/api/insumos").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(25))
                .andExpect(jsonPath("$.totalPages").value(3));

        mockMvc.perform(get("/api/insumos").param("page", "2").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("GET /api/insumos deve respeitar sort=nome,desc")
    @WithMockUser(roles = "GESTOR")
    void listarInsumosOrdenado() throws Exception {
        insumoRepository.save(Insumo.builder().nome("Agulha").quantidadeAtual(5).nivelCritico(1).build());
        insumoRepository.save(Insumo.builder().nome("Zinco").quantidadeAtual(5).nivelCritico(1).build());

        mockMvc.perform(get("/api/insumos").param("sort", "nome,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Zinco"));
    }

    @Test
    @DisplayName("Alias legado /api/insumos/paginado deve retornar o mesmo contrato Page")
    @WithMockUser(roles = "GESTOR")
    void aliasPaginadoMantido() throws Exception {
        mockMvc.perform(get("/api/insumos/paginado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("GET /api/agendamentos deve retornar Page")
    @WithMockUser(roles = "GESTOR")
    void listarAgendamentosRetornaPagina() throws Exception {
        mockMvc.perform(get("/api/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @DisplayName("GET /api/financeiro/transacoes deve retornar Page")
    @WithMockUser(roles = "GESTOR")
    void listarTransacoesRetornaPagina() throws Exception {
        mockMvc.perform(get("/api/financeiro/transacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }
}
