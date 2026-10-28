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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ComissaoControllerTest {

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

    @Test
    @DisplayName("GET /tatuador/{id}/fechamento/export deve retornar CSV com cabeçalho e linha da transação")
    @WithMockUser(roles = "GESTOR")
    void exportarCsvComTransacoes() throws Exception {
        transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao("Sessão Realismo")
                .valor(new BigDecimal("800.00"))
                .tipo(TipoTransacao.RECEITA)
                .data(LocalDate.now())
                .tatuador(tatuador)
                .categoria("Procedimento Tattoo")
                .build());

        mockMvc.perform(get("/api/comissoes/tatuador/" + tatuador.getId() + "/fechamento/export")
                        .param("inicio", LocalDate.now().minusDays(1).toString())
                        .param("fim", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=commissions.csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data;valor;tipo;categoria;tatuadorId")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("800")));
    }

    @Test
    @DisplayName("Export para tatuador inexistente deve retornar 200 apenas com cabeçalho (sem transações)")
    @WithMockUser(roles = "GESTOR")
    void exportarCsvTatuadorInexistente() throws Exception {
        mockMvc.perform(get("/api/comissoes/tatuador/99999/fechamento/export")
                        .param("inicio", "2026-01-01")
                        .param("fim", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().string("data;valor;tipo;categoria;tatuadorId"));
    }

    @Test
    @DisplayName("Export deve retornar 403 para CLIENTE")
    @WithMockUser(roles = "CLIENTE")
    void exportarCsvProibidoParaCliente() throws Exception {
        mockMvc.perform(get("/api/comissoes/tatuador/" + tatuador.getId() + "/fechamento/export")
                        .param("inicio", "2026-01-01")
                        .param("fim", "2026-12-31"))
                .andExpect(status().isForbidden());
    }
}
