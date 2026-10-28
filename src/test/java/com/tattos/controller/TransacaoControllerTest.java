package com.tattos.controller;

import com.tattos.model.Tatuador;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        tatuador = Tatuador.builder()
                .nome("Diego Ortiz")
                .email("diego@tattos.com")
                .senha("pass123")
                .comissaoPercentual(new BigDecimal("60.00"))
                .ativo(true)
                .build();
        tatuador = (Tatuador) usuarioRepository.save(tatuador);
    }

    private TransacaoFinanceira novaTransacao(String descricao, String valor) {
        return transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao(descricao)
                .valor(new BigDecimal(valor))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuador(tatuador)
                .categoria("Procedimento Tattoo")
                .build());
    }

    @Test
    @DisplayName("GET /api/financeiro/transacoes deve retornar Page de transações")
    @WithMockUser(roles = "GESTOR")
    void listarRetornaPagina() throws Exception {
        novaTransacao("Sessão 1", "500.00");
        novaTransacao("Sessão 2", "700.00");

        mockMvc.perform(get("/api/financeiro/transacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /transacoes/{id} deve retornar 200 e 404 para inexistente")
    @WithMockUser(roles = "GESTOR")
    void buscarPorId() throws Exception {
        TransacaoFinanceira t = novaTransacao("Sinal Reserva", "300.00");

        mockMvc.perform(get("/api/financeiro/transacoes/" + t.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Sinal Reserva"));

        mockMvc.perform(get("/api/financeiro/transacoes/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /transacoes com dados válidos deve retornar 201 com repasse calculado")
    @WithMockUser(roles = "GESTOR")
    void criarValido() throws Exception {
        String payload = """
                {"descricao": "Tatuagem Braço", "valor": 2000.00, "tipo": "RECEITA",
                 "data": "%s", "tatuadorId": %d}
                """.formatted(LocalDate.now(), tatuador.getId());

        mockMvc.perform(post("/api/financeiro/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorRepasse").value(1200.00));
    }

    @Test
    @DisplayName("POST /transacoes com body inválido deve retornar 400 com fieldErrors")
    @WithMockUser(roles = "GESTOR")
    void criarInvalido() throws Exception {
        mockMvc.perform(post("/api/financeiro/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descricao\": \"\", \"valor\": -5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("GET /balanco deve retornar 200 para GESTOR")
    @WithMockUser(roles = "GESTOR")
    void balanco() throws Exception {
        novaTransacao("Sessão", "1000.00");

        mockMvc.perform(get("/api/financeiro/balanco"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /balanco deve retornar 403 para TATUADOR")
    @WithMockUser(roles = "TATUADOR")
    void balancoProibidoParaTatuador() throws Exception {
        mockMvc.perform(get("/api/financeiro/balanco"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /repasses/{tatuadorId} deve retornar total de repasse")
    @WithMockUser(roles = "GESTOR")
    void repasses() throws Exception {
        novaTransacao("Sessão", "1000.00");

        mockMvc.perform(get("/api/financeiro/repasses/" + tatuador.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tatuadorId").value(tatuador.getId()))
                .andExpect(jsonPath("$.totalRepasse").exists());
    }

    @Test
    @DisplayName("GET /comissoes/{tatuadorId}/fechamento deve retornar demonstrativo")
    @WithMockUser(roles = "GESTOR")
    void fechamentoComissoes() throws Exception {
        novaTransacao("Sessão", "1000.00");

        mockMvc.perform(get("/api/financeiro/comissoes/" + tatuador.getId() + "/fechamento"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /transacoes/{id} deve retornar 204 e 404 para inexistente")
    @WithMockUser(roles = "GESTOR")
    void deletar() throws Exception {
        TransacaoFinanceira t = novaTransacao("A remover", "100.00");

        mockMvc.perform(delete("/api/financeiro/transacoes/" + t.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/financeiro/transacoes/99999"))
                .andExpect(status().isNotFound());
    }
}
