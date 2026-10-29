package com.tattos.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clientCannotReadAllUsers() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clientCannotReadClientDirectory() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/clientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clientCannotReadAnotherUserById() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/11"))
                .andExpect(status().isForbidden());
    }
}
