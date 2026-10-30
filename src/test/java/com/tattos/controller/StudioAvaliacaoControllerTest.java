package com.tattos.controller;

import com.tattos.model.Avaliacao;
import com.tattos.model.Cliente;
import com.tattos.model.Tatuador;
import com.tattos.model.Trabalho;
import com.tattos.model.Usuario;
import com.tattos.repository.AvaliacaoRepository;
import com.tattos.repository.TrabalhoRepository;
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
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudioAvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private TrabalhoRepository trabalhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Cliente cliente;
    private Cliente outroCliente;
    private Tatuador tatuador;
    private Trabalho trabalhoDoCliente;
    private Trabalho trabalhoPortfolio;

    @BeforeEach
    void setUp() {
        avaliacaoRepository.deleteAll();
        trabalhoRepository.deleteAll();
        usuarioRepository.deleteAll();

        cliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Cli Aval").email("aval.c@tattos.com").senha("h").ativo(true).build());
        outroCliente = (Cliente) usuarioRepository.save(Cliente.builder()
                .nome("Outro Cli").email("aval.o@tattos.com").senha("h").ativo(true).build());
        tatuador = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Tat Aval").email("aval.t@tattos.com").senha("h")
                .comissaoPercentual(new BigDecimal("60")).ativo(true).build());

        trabalhoDoCliente = trabalhoRepository.save(Trabalho.builder()
                .tatuador(tatuador).cliente(cliente).titulo("Carpa do cliente")
                .estilo("Oriental").data(LocalDate.now().minusDays(5)).build());
        trabalhoPortfolio = trabalhoRepository.save(Trabalho.builder()
                .tatuador(tatuador).titulo("Flash solto").data(LocalDate.now()).build());
    }

    private String token(Usuario u) {
        return "Bearer " + jwtTokenProvider.generateTokenForUser(u);
    }

    @Test
    @DisplayName("POST avaliacao feliz retorna 201 com dados do trabalho")
    void fluxoFeliz() throws Exception {
        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoDoCliente.getId()
                                + ", \"notaTatuagem\": 5, \"notaTatuador\": 4, \"comentario\": \"amei\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.trabalhoId").value(trabalhoDoCliente.getId()))
                .andExpect(jsonPath("$.tatuadorNome").value("Tat Aval"))
                .andExpect(jsonPath("$.clienteNome").value("Cli Aval"))
                .andExpect(jsonPath("$.notaTatuagem").value(5))
                .andExpect(jsonPath("$.data").value(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("POST sem token retorna 401/403")
    void semToken() throws Exception {
        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoDoCliente.getId()
                                + ", \"notaTatuagem\": 5, \"notaTatuador\": 4}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GESTOR nao pode avaliar (403)")
    @WithMockUser(roles = "GESTOR")
    void gestorNaoAvalia() throws Exception {
        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoDoCliente.getId()
                                + ", \"notaTatuagem\": 5, \"notaTatuador\": 4}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Avaliar trabalho de outro cliente retorna 403")
    void avaliarDeOutroCliente403() throws Exception {
        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .header("Authorization", token(outroCliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoDoCliente.getId()
                                + ", \"notaTatuagem\": 5, \"notaTatuador\": 4}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Avaliacao duplicada retorna 409")
    void avaliacaoDuplicada409() throws Exception {
        avaliacaoRepository.save(Avaliacao.builder().trabalho(trabalhoDoCliente)
                .cliente(cliente).tatuador(tatuador)
                .notaTatuagem(5).notaTatuador(5).data(LocalDate.now()).build());

        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoDoCliente.getId()
                                + ", \"notaTatuagem\": 4, \"notaTatuador\": 4}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Avaliar trabalho de portfolio (sem cliente) retorna 422/400")
    void avaliarPortfolioSemCliente() throws Exception {
        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoPortfolio.getId()
                                + ", \"notaTatuagem\": 5, \"notaTatuador\": 4}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Nota fora de 1-5 retorna 400")
    void notaInvalida400() throws Exception {
        mockMvc.perform(post("/api/v1/studio/avaliacoes")
                        .header("Authorization", token(cliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trabalhoId\": " + trabalhoDoCliente.getId()
                                + ", \"notaTatuagem\": 6, \"notaTatuador\": 4}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET pendentes lista trabalhos do cliente ainda nao avaliados")
    void pendentes() throws Exception {
        mockMvc.perform(get("/api/v1/studio/avaliacoes/pendentes")
                        .header("Authorization", token(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(trabalhoDoCliente.getId()))
                .andExpect(jsonPath("$[0].tatuadorNome").value("Tat Aval"))
                .andExpect(jsonPath("$[0].dataConclusao").value(trabalhoDoCliente.getData().toString()));
    }

    @Test
    @DisplayName("GET minhas lista avaliacoes enviadas pelo cliente")
    void minhas() throws Exception {
        avaliacaoRepository.save(Avaliacao.builder().trabalho(trabalhoDoCliente)
                .cliente(cliente).tatuador(tatuador)
                .notaTatuagem(5).notaTatuador(4).comentario("top").data(LocalDate.now()).build());

        mockMvc.perform(get("/api/v1/studio/avaliacoes/minhas")
                        .header("Authorization", token(cliente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].comentario").value("top"));
    }

    @Test
    @DisplayName("GET avaliacoes geral lista mais recentes primeiro")
    @WithMockUser(roles = "CLIENTE")
    void geral() throws Exception {
        avaliacaoRepository.save(Avaliacao.builder().trabalho(trabalhoDoCliente)
                .cliente(cliente).tatuador(tatuador)
                .notaTatuagem(5).notaTatuador(4).data(LocalDate.now()).build());

        mockMvc.perform(get("/api/v1/studio/avaliacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trabalhoId").value(trabalhoDoCliente.getId()));
    }
}
