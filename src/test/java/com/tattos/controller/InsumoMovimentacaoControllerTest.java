package com.tattos.controller;

import com.tattos.model.Insumo;
import com.tattos.model.Tatuador;
import com.tattos.repository.InsumoRepository;
import com.tattos.repository.MovimentacaoEstoqueRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InsumoMovimentacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long insumoId;
    private Long tatuadorId;

    @BeforeEach
    void setUp() {
        movimentacaoRepository.deleteAll();
        insumoRepository.deleteAll();
        usuarioRepository.deleteAll();

        insumoId = insumoRepository.save(Insumo.builder()
                .nome("Agulha 03RL").quantidadeAtual(10).nivelCritico(2)
                .unidadeMedida("CX").valorUnitario(new BigDecimal("2.00")).build()).getId();
        tatuadorId = usuarioRepository.save(Tatuador.builder()
                .nome("Tat Mov").email("mov.t@tattos.com").senha("h").ativo(true).build()).getId();
    }

    @Test
    @DisplayName("POST entrada com custo retorna 200 e atualiza o valor medio")
    @WithMockUser(roles = "GESTOR")
    void postEntradaComCustoRetorna200EAtualizaValorMedio() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/" + insumoId + "/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 10, \"custoUnitario\": 4.00, \"motivo\": \"reposição\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeAtual").value(20))
                .andExpect(jsonPath("$.valorUnitario").value(4.00));
    }

    @Test
    @DisplayName("POST saida sem retiradoPorId retorna 400")
    @WithMockUser(roles = "GESTOR")
    void postSaidaSemRetiradoPorIdRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/" + insumoId + "/saida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET movimentacoes lista as mais recentes primeiro")
    @WithMockUser(roles = "GESTOR")
    void getMovimentacoesListaMaisRecentesPrimeiro() throws Exception {
        mockMvc.perform(post("/api/v1/estoque/" + insumoId + "/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 5, \"custoUnitario\": 4.00}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/estoque/" + insumoId + "/saida")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 2, \"retiradoPorId\": " + tatuadorId + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/estoque/" + insumoId + "/movimentacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("SAIDA"))
                .andExpect(jsonPath("$[0].retiradoPorNome").value("Tat Mov"))
                .andExpect(jsonPath("$[1].tipo").value("ENTRADA"));
    }

    @Test
    @DisplayName("PATCH antigo de entrada continua funcionando")
    @WithMockUser(roles = "GESTOR")
    void patchAntigoContinuaFuncionando() throws Exception {
        mockMvc.perform(patch("/api/v1/estoque/" + insumoId + "/entrada")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeAtual").value(13));
    }

    @Test
    @DisplayName("Cliente nao acessa movimentacoes (403)")
    @WithMockUser(roles = "CLIENTE")
    void clienteNaoAcessaMovimentacoesRetorna403() throws Exception {
        mockMvc.perform(get("/api/v1/estoque/" + insumoId + "/movimentacoes"))
                .andExpect(status().isForbidden());
    }
}
