package com.tattos.controller;

import com.tattos.repository.StudioInfoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StudioInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudioInfoRepository studioInfoRepository;

    @BeforeEach
    void setUp() {
        studioInfoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /studio autenticado retorna info (criada por padrao se ausente)")
    @WithMockUser(roles = "CLIENTE")
    void getAutenticadoRetorna200() throws Exception {
        mockMvc.perform(get("/api/v1/studio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").exists());
    }

    @Test
    @DisplayName("GET /studio sem token retorna 401/403")
    void getSemTokenNaoAutorizado() throws Exception {
        mockMvc.perform(get("/api/v1/studio"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PATCH /studio como TATUADOR retorna 403")
    @WithMockUser(roles = "TATUADOR")
    void patchTatuadorProibido() throws Exception {
        mockMvc.perform(patch("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"X\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /studio com body vazio nao altera nada")
    @WithMockUser(roles = "GESTOR")
    void patchVazioNaoAltera() throws Exception {
        mockMvc.perform(patch("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Studio Legal\", \"instagram\": \"@studio\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Studio Legal"))
                .andExpect(jsonPath("$.instagram").value("@studio"));
    }

    @Test
    @DisplayName("PATCH /studio como GESTOR atualiza e GET reflete")
    @WithMockUser(roles = "GESTOR")
    void patchGestorAtualiza() throws Exception {
        mockMvc.perform(patch("/api/v1/studio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Novo Nome\", \"horario\": \"10h-18h\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo Nome"))
                .andExpect(jsonPath("$.horario").value("10h-18h"));
    }
}
