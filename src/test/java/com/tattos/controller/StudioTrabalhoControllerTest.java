package com.tattos.controller;

import com.tattos.model.Tatuador;
import com.tattos.model.Trabalho;
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
class StudioTrabalhoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrabalhoRepository trabalhoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Tatuador tatA;
    private Tatuador tatB;

    @BeforeEach
    void setUp() {
        trabalhoRepository.deleteAll();
        usuarioRepository.deleteAll();

        tatA = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Mãozinha Leve").email("st.a@tattos.com").senha("h")
                .especialidade("Fine Line").comissaoPercentual(new BigDecimal("60")).ativo(true).build());
        tatB = (Tatuador) usuarioRepository.save(Tatuador.builder()
                .nome("Bracinho Curto").email("st.b@tattos.com").senha("h")
                .especialidade("Old School").comissaoPercentual(new BigDecimal("60")).ativo(true).build());
    }

    private String token(com.tattos.model.Usuario u) {
        return "Bearer " + jwtTokenProvider.generateTokenForUser(u);
    }

    private String body(Long tatuadorId, String titulo) {
        return "{\"tatuadorId\": " + tatuadorId + ", \"titulo\": \"" + titulo
                + "\", \"estilo\": \"Fine Line\", \"descricao\": \"desc\", \"fotoUrl\": \"https://img/x.jpg\"}";
    }

    @Test
    @DisplayName("Gestor posta trabalho de qualquer tatuador (201)")
    @WithMockUser(roles = "GESTOR")
    void gestorPostaParaQualquerTatuador() throws Exception {
        mockMvc.perform(post("/api/v1/studio/trabalhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(tatB.getId(), "Dragão")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tatuadorId").value(tatB.getId()))
                .andExpect(jsonPath("$.tatuadorNome").value("Bracinho Curto"))
                .andExpect(jsonPath("$.data").value(LocalDate.now().toString()));
    }

    @Test
    @DisplayName("Tatuador posta trabalho dele mesmo (201)")
    void tatuadorPostaProprio() throws Exception {
        mockMvc.perform(post("/api/v1/studio/trabalhos")
                        .header("Authorization", token(tatA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(tatA.getId(), "Carpa")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Tatuador postando trabalho de outro tatuador retorna 403")
    void tatuadorPostandoDeOutroRetorna403() throws Exception {
        mockMvc.perform(post("/api/v1/studio/trabalhos")
                        .header("Authorization", token(tatA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(tatB.getId(), "Carpa")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST sem token retorna 401/403")
    void semTokenNaoAutorizado() throws Exception {
        mockMvc.perform(post("/api/v1/studio/trabalhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(tatA.getId(), "X")))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST com titulo em branco retorna 400 com fieldErrors")
    @WithMockUser(roles = "GESTOR")
    void tituloBrancoRetorna400() throws Exception {
        mockMvc.perform(post("/api/v1/studio/trabalhos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(tatA.getId(), "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists());
    }

    @Test
    @DisplayName("GET trabalhos lista mais recentes primeiro")
    @WithMockUser(roles = "CLIENTE")
    void getOrdenadoPorDataDesc() throws Exception {
        trabalhoRepository.save(Trabalho.builder().tatuador(tatA).titulo("Antigo")
                .data(LocalDate.now().minusDays(10)).build());
        trabalhoRepository.save(Trabalho.builder().tatuador(tatB).titulo("Recente")
                .data(LocalDate.now()).build());

        mockMvc.perform(get("/api/v1/studio/trabalhos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Recente"))
                .andExpect(jsonPath("$[1].titulo").value("Antigo"));
    }
}
