package com.tattos.controller;

import com.tattos.model.IdeiaTatuagem;
import com.tattos.repository.IdeiaTatuagemRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudioIdeiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IdeiaTatuagemRepository ideiaRepository;

    @BeforeEach
    void setUp() {
        ideiaRepository.deleteAll();
    }

    @Test
    @DisplayName("POST ideia valida retorna 201")
    @WithMockUser(roles = "TATUADOR")
    void postIdeia201() throws Exception {
        mockMvc.perform(post("/api/v1/studio/ideias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Dragão minimalista\", \"estilo\": \"Fine Line\", \"imagemUrl\": \"https://img/d.jpg\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Dragão minimalista"))
                .andExpect(jsonPath("$.imagemUrl").value("https://img/d.jpg"));
    }

    @Test
    @DisplayName("POST ideia com titulo em branco retorna 400")
    @WithMockUser(roles = "GESTOR")
    void tituloBranco400() throws Exception {
        mockMvc.perform(post("/api/v1/studio/ideias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.titulo").exists());
    }

    @Test
    @DisplayName("CLIENTE nao pode postar ideia (403)")
    @WithMockUser(roles = "CLIENTE")
    void clienteProibido() throws Exception {
        mockMvc.perform(post("/api/v1/studio/ideias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST ideia sem token retorna 401/403")
    void semToken() throws Exception {
        mockMvc.perform(post("/api/v1/studio/ideias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"X\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET ideias lista todas (autenticado)")
    @WithMockUser(roles = "CLIENTE")
    void getLista() throws Exception {
        ideiaRepository.save(IdeiaTatuagem.builder().titulo("Cobra floral").build());
        mockMvc.perform(get("/api/v1/studio/ideias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Cobra floral"));
    }
}
