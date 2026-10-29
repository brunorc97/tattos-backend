package com.tattos.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Upload de PDF válido retorna 201 com id e url")
    @WithMockUser(roles = "GESTOR")
    void uploadPdfValido() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "comprovante.pdf", "application/pdf", "%PDF-1.4 fake".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.nomeOriginal").value("comprovante.pdf"));
    }

    @Test
    @DisplayName("Upload de executável retorna 400")
    @WithMockUser(roles = "GESTOR")
    void uploadExeRejeitado() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "MZ".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Upload sem autenticação retorna 401")
    void uploadSemAuth() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "%PDF".getBytes());

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET de arquivo inexistente retorna 404")
    @WithMockUser(roles = "GESTOR")
    void downloadInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/files/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Cliente não baixa arquivo de outro usuário (403)")
    void downloadDeOutroUsuarioProibido() throws Exception {
        // seed: dono do arquivo
        com.tattos.model.Cliente dono = (com.tattos.model.Cliente) usuarioRepository.save(
                com.tattos.model.Cliente.builder().nome("Dono").email("dono@x.com").senha("h").ativo(true).build());
        com.tattos.model.Cliente intruso = (com.tattos.model.Cliente) usuarioRepository.save(
                com.tattos.model.Cliente.builder().nome("Intruso").email("intruso@x.com").senha("h").ativo(true).build());

        // upload como dono (via service para não depender de disco)
        org.springframework.mock.web.MockMultipartFile pdf = new org.springframework.mock.web.MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "%PDF".getBytes());
        com.tattos.model.Arquivo arquivo = fileStorageService.salvar(pdf, dono);

        String tokenIntruso = "Bearer " + jwtTokenProvider.generateTokenForUser(intruso);
        mockMvc.perform(get("/api/v1/files/" + arquivo.getId())
                        .header("Authorization", tokenIntruso))
                .andExpect(status().isForbidden());
    }

    @Autowired
    private com.tattos.repository.UsuarioRepository usuarioRepository;

    @Autowired
    private com.tattos.service.FileStorageService fileStorageService;

    @Autowired
    private com.tattos.security.JwtTokenProvider jwtTokenProvider;
}
