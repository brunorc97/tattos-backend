package com.tattos.controller;

import com.tattos.model.Tatuador;
import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.enums.TipoTransacao;
import com.tattos.repository.PagamentoComissaoRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PagamentoComissaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TransacaoFinanceiraRepository transacaoRepository;

    @Autowired
    private PagamentoComissaoRepository pagamentoRepository;

    private Tatuador tatuador;
    private Long t1;
    private Long t2;

    @BeforeEach
    void setUp() {
        pagamentoRepository.deleteAll();
        transacaoRepository.deleteAll();
        usuarioRepository.deleteAll();

        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Comissao").email("comissao.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        t1 = transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao("Trabalho 1").valor(new BigDecimal("1000.00"))
                .tipo(TipoTransacao.RECEITA).data(LocalDate.now())
                .tatuador(tatuador).build()).getId();
        t2 = transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao("Trabalho 2").valor(new BigDecimal("500.00"))
                .tipo(TipoTransacao.RECEITA).data(LocalDate.now())
                .tatuador(tatuador).build()).getId();
    }

    @Test
    @DisplayName("Gestor lista trabalhos pendentes de repasse do tatuador")
    @WithMockUser(roles = "GESTOR")
    void listaTrabalhosPendentes() throws Exception {
        mockMvc.perform(get("/api/v1/financeiro/comissoes/" + tatuador.getId() + "/trabalhos")
                        .param("statusRepasse", "PENDENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("Fluxo completo: selecionar trabalhos → AGENDADO → confirmar → PAGO")
    @WithMockUser(roles = "GESTOR")
    void fluxoCompleto() throws Exception {
        // repasses: 60% de 1000 = 600; 60% de 500 = 300 → bruto 900
        mockMvc.perform(post("/api/v1/financeiro/pagamentos-comissao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + tatuador.getId()
                                + ", \"transacaoIds\": [" + t1 + ", " + t2 + "], \"formaPagamento\": \"PIX\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.totalBruto").value(900.00))
                .andExpect(jsonPath("$.totalLiquido").value(900.00))
                .andExpect(jsonPath("$.totalAdiantamentosAbatidos").value(0.00));

        Long pagamentoId = pagamentoRepository.findAll().get(0).getId();

        // re-selecionar transação já agendada → 409
        mockMvc.perform(post("/api/v1/financeiro/pagamentos-comissao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + tatuador.getId()
                                + ", \"transacaoIds\": [" + t1 + "], \"formaPagamento\": \"PIX\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/financeiro/pagamentos-comissao/" + pagamentoId + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAGO"));

        // após pago, transações marcadas como PAGO
        org.assertj.core.api.Assertions.assertThat(
                transacaoRepository.findById(t1).orElseThrow().getStatusRepasse().name()).isEqualTo("PAGO");
    }

    @Test
    @DisplayName("Selecionar transação de outro tatuador retorna 400")
    @WithMockUser(roles = "GESTOR")
    void transacaoDeOutroTatuador() throws Exception {
        Tatuador outro = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Outro").email("comissao.o@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        Long alheio = transacaoRepository.save(TransacaoFinanceira.builder()
                .descricao("Do outro").valor(new BigDecimal("100.00"))
                .tipo(TipoTransacao.RECEITA).data(LocalDate.now())
                .tatuador(outro).build()).getId();

        mockMvc.perform(post("/api/v1/financeiro/pagamentos-comissao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tatuadorId\": " + tatuador.getId()
                                + ", \"transacaoIds\": [" + alheio + "], \"formaPagamento\": \"DINHEIRO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Recibo de pagamento inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void reciboInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/financeiro/pagamentos-comissao/99999/recibo.pdf"))
                .andExpect(status().isNotFound());
    }
}
