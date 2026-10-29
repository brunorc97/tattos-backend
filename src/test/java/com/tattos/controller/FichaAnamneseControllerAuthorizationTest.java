package com.tattos.controller;

import com.tattos.dto.response.FichaAnamneseResponseDTO;
import com.tattos.model.Cliente;
import com.tattos.model.Usuario;
import com.tattos.service.FichaAnamneseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FichaAnamneseControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FichaAnamneseService fichaService;

    @Test
    void clientCannotQueryAnotherClientsAnamnesis() throws Exception {
        mockMvc.perform(get("/api/v1/fichas-anamnese/cliente/22")
                        .with(authenticatedClient(11L)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(fichaService);
    }

    @Test
    void clientCanQueryOwnAnamnesis() throws Exception {
        when(fichaService.buscarPorCliente(11L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/fichas-anamnese/cliente/11")
                        .with(authenticatedClient(11L)))
                .andExpect(status().isOk());

        verify(fichaService).buscarPorCliente(11L);
    }

    @Test
    void clientCannotReadOrSignAnotherClientsAnamnesis() throws Exception {
        when(fichaService.buscarPorId(41L)).thenReturn(
                FichaAnamneseResponseDTO.builder().id(41L).clienteId(22L).build());

        mockMvc.perform(get("/api/v1/fichas-anamnese/41")
                        .with(authenticatedClient(11L)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/fichas-anamnese/41/assinatura")
                        .with(authenticatedClient(11L))
                        .contentType("application/json")
                        .content("{\"email\":\"cliente@example.com\",\"usuarioId\":11}"))
                .andExpect(status().isForbidden());

        verify(fichaService, never()).assinarTermo(org.mockito.ArgumentMatchers.eq(41L), org.mockito.ArgumentMatchers.any());
    }

    private RequestPostProcessor authenticatedClient(Long id) {
        Usuario usuario = new Cliente();
        usuario.setId(id);
        usuario.setEmail("client-" + id + "@example.com");
        usuario.setSenha("password");
        usuario.syncRole();
        return authentication(UsernamePasswordAuthenticationToken.authenticated(
                usuario, usuario.getPassword(), usuario.getAuthorities()));
    }
}
