package com.tattos.controller;

import com.tattos.model.Tatuador;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.Usuario;
import com.tattos.model.enums.StatusRepasse;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.TransacaoFinanceiraRepository;
import com.tattos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FechamentoComissaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        transacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Fechamento").email("fechamento.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao("Trabalho pendente").valor(new BigDecimal("1000.00"))
                .tipo(TipoTransacao.RECEITA).data(LocalDate.of(2026, 10, 10))
                .tatuador(tatuador).comissaoPercentual(new BigDecimal("60"))
                .valorRepasse(new BigDecimal("600.00"))
                .statusRepasse(StatusRepasse.PENDENTE).build());
        transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao("Trabalho pago").valor(new BigDecimal("500.00"))
                .tipo(TipoTransacao.RECEITA).data(LocalDate.of(2026, 10, 20))
                .tatuador(tatuador).comissaoPercentual(new BigDecimal("60"))
                .valorRepasse(new BigDecimal("300.00"))
                .statusRepasse(StatusRepasse.PAGO).build());
    }

    private RequestPostProcessor authenticatedTatuador(Long id) {
        Tatuador u = new Tatuador();
        u.setId(id);
        u.setEmail("tat-" + id + "@example.com");
        u.setSenha("password");
        u.syncRole();
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                u, u.getPassword(), u.getAuthorities()));
    }

    @Test
    @DisplayName("GET fechamento retorna itens com statusRepasse")
    void fechamentoRetornaItensComStatusRepasse() throws Exception {
        mockMvc.perform(get("/api/v1/comissoes/tatuador/" + tatuador.getId() + "/fechamento")
                        .param("inicio", "2026-10-01").param("fim", "2026-10-31")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("g").roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tatuadorId").value(tatuador.getId()))
                .andExpect(jsonPath("$.totalAtendimentos").value(2))
                .andExpect(jsonPath("$.itens.length()").value(2))
                .andExpect(jsonPath("$.itens[?(@.statusRepasse == 'PENDENTE')]").exists())
                .andExpect(jsonPath("$.itens[?(@.statusRepasse == 'PAGO')]").exists());
    }

    @Test
    @DisplayName("GET fechamento sem transações retorna totais zerados")
    void fechamentoSemTransacoesRetornaZerado() throws Exception {
        mockMvc.perform(get("/api/v1/comissoes/tatuador/" + tatuador.getId() + "/fechamento")
                        .param("inicio", "2025-01-01").param("fim", "2025-01-31")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("g").roles("GESTOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtendimentos").value(0))
                .andExpect(jsonPath("$.itens.length()").value(0));
    }

    @Test
    @DisplayName("GET fechamento com inicio maior que fim retorna 400")
    void fechamentoComInicioMaiorQueFimRetorna400() throws Exception {
        mockMvc.perform(get("/api/v1/comissoes/tatuador/" + tatuador.getId() + "/fechamento")
                        .param("inicio", "2026-10-31").param("fim", "2026-10-01")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("g").roles("GESTOR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Tatuador pedindo fechamento de outro tatuador retorna 403")
    void tatuadorPedindoFechamentoDeOutroRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/comissoes/tatuador/" + tatuador.getId() + "/fechamento")
                        .param("inicio", "2026-10-01").param("fim", "2026-10-31")
                        .with(authenticatedTatuador(tatuador.getId() + 999)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Tatuador vendo o próprio fechamento retorna 200")
    void tatuadorVeProprioFechamento() throws Exception {
        mockMvc.perform(get("/api/v1/comissoes/tatuador/" + tatuador.getId() + "/fechamento")
                        .param("inicio", "2026-10-01").param("fim", "2026-10-31")
                        .with(authenticatedTatuador(tatuador.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAtendimentos").value(2));
    }
}
