package com.tattos.controller;

import com.tattos.model.Cliente;
import com.tattos.model.Orcamento;
import com.tattos.model.Projeto;
import com.tattos.model.Tatuador;
import com.tattos.model.enums.StatusAprovacaoProjeto;
import com.tattos.repository.OrcamentoRepository;
import com.tattos.repository.ProjetoRepository;
import com.tattos.repository.UsuarioRepository;
import com.tattos.security.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrcamentoStatusTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Orcamento orcamento;
    private Projeto projeto;
    private Cliente cliente;
    private Tatuador tatuador;

    @BeforeEach
    void setUp() {
        orcamentoRepository.deleteAll();
        projetoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Orc").email("orc.c@tattos.com").senha("h").ativo(true).build());
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Orc").email("orc.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        projeto = projetoRepository.save(Projeto.builder()
                .nome("Dragão costas").cliente(cliente).tatuador(tatuador).build());
        orcamento = orcamentoRepository.save(Orcamento.builder()
                .nome("Dragão 20cm").valor(new BigDecimal("1300.00")).projeto(projeto).build());
    }

    private String token(com.tattos.model.Usuario u) {
        return "Bearer " + jwtTokenProvider.generateTokenForUser(u);
    }

    private org.springframework.test.web.servlet.ResultActions mudarStatus(Long id, String status, String auth) throws Exception {
        return mockMvc.perform(patch("/api/v1/orcamentos/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", auth)
                .content("{\"status\": \"" + status + "\"}"));
    }

    @Test
    @DisplayName("Fluxo feliz: RASCUNHO -> ENVIADO (tatuador dono) -> ACEITO (cliente dono) aprova projeto")
    void fluxoFeliz() throws Exception {
        mudarStatus(orcamento.getId(), "ENVIADO", token(tatuador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));

        mudarStatus(orcamento.getId(), "ACEITO", token(cliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITO"));

        org.assertj.core.api.Assertions.assertThat(
                projetoRepository.findById(projeto.getId()).orElseThrow().getStatusAprovacao())
                .isEqualTo(StatusAprovacaoProjeto.APROVADO);
    }

    @Test
    @DisplayName("Transicao invalida RASCUNHO -> ACEITO retorna 409")
    void transicaoInvalida() throws Exception {
        mudarStatus(orcamento.getId(), "ACEITO", token(cliente))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Cliente de outro projeto nao pode aceitar (403)")
    void clienteAlheioNaoAceita() throws Exception {
        Cliente outro = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Outro").email("orc.o@tattos.com").senha("h").ativo(true).build());
        orcamento.setStatus(com.tattos.model.enums.StatusOrcamento.ENVIADO);
        orcamentoRepository.save(orcamento);

        mudarStatus(orcamento.getId(), "ACEITO", token(outro))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Gestor pode enviar orcamento")
    @WithMockUser(roles = "GESTOR")
    void gestorEnvia() throws Exception {
        mockMvc.perform(patch("/api/v1/orcamentos/" + orcamento.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"ENVIADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));
    }
}
