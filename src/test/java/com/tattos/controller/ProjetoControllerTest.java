package com.tattos.controller;

import com.tattos.model.Projeto;
import com.tattos.repository.ProjetoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProjetoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjetoRepository projetoRepository;

    @BeforeEach
    void setUp() {
        projetoRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/projetos deve retornar 200 com lista")
    @WithMockUser(roles = "GESTOR")
    void listar() throws Exception {
        projetoRepository.save(Projeto.builder().nome("Projeto Capa de Costas").build());

        mockMvc.perform(get("/api/projetos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Projeto Capa de Costas"));
    }

    @Test
    @DisplayName("POST /api/projetos válido deve retornar 201")
    @WithMockUser(roles = "GESTOR")
    void criarValido() throws Exception {
        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Projeto Oriental\", \"descricao\": \"Manga inteira\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nome").value("Projeto Oriental"));
    }

    @Test
    @DisplayName("POST /{id}/aprovar deve marcar o projeto como aprovado")
    @WithMockUser(roles = "GESTOR")
    void aprovarProjeto() throws Exception {
        Projeto projeto = projetoRepository.save(Projeto.builder().nome("Projeto X").build());

        mockMvc.perform(post("/api/projetos/" + projeto.getId() + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aprovado").value(true));

        assertThat(projetoRepository.findById(projeto.getId())).get()
                .extracting(Projeto::isAprovado).isEqualTo(true);
    }

    @Test
    @DisplayName("POST /{id}/aprovar com id inexistente deve retornar 404")
    @WithMockUser(roles = "GESTOR")
    void aprovarProjetoInexistente() throws Exception {
        mockMvc.perform(post("/api/projetos/99999/aprovar"))
                .andExpect(status().isNotFound());
    }
}
