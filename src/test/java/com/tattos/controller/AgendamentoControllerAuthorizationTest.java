package com.tattos.controller;

import com.tattos.dto.request.AgendamentoRequestDTO;
import com.tattos.dto.response.AgendamentoResponseDTO;
import com.tattos.model.Cliente;
import com.tattos.model.Usuario;
import com.tattos.service.AgendamentoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgendamentoControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgendamentoService agendamentoService;

    @Test
    void clientListIsAlwaysRestrictedToAuthenticatedClient() throws Exception {
        when(agendamentoService.listarPaginado(any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/api/v1/agendamentos")
                        .param("clienteId", "22")
                        .with(authenticatedClient(11L)))
                .andExpect(status().isOk());

        verify(agendamentoService).listarPaginado(isNull(), eq(11L), isNull(), isNull(), any());
    }

    @Test
    void clientCannotReadAnotherClientsAppointment() throws Exception {
        when(agendamentoService.buscarPorId(41L)).thenReturn(
                AgendamentoResponseDTO.builder().id(41L).clienteId(22L).build());

        mockMvc.perform(get("/api/v1/agendamentos/41")
                        .with(authenticatedClient(11L)))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCanOnlyCreateAnAppointmentForTheirOwnAccount() throws Exception {
        when(agendamentoService.criar(any(AgendamentoRequestDTO.class))).thenAnswer(invocation ->
                AgendamentoResponseDTO.builder()
                        .id(41L)
                        .clienteId(((AgendamentoRequestDTO) invocation.getArgument(0)).getClienteId())
                        .build());

        mockMvc.perform(post("/api/v1/agendamentos")
                        .with(authenticatedClient(11L))
                        .contentType("application/json")
                        .content("""
                                {
                                  "clienteId": 22,
                                  "tatuadorId": 3,
                                  "dataHora": "2099-11-15T14:00:00",
                                  "duracaoMinutos": 120,
                                  "cabine": "A1",
                                  "valor": 1500.00,
                                  "valorSinal": 300.00
                                }
                                """))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<AgendamentoRequestDTO> request =
                                org.mockito.ArgumentCaptor.forClass(AgendamentoRequestDTO.class);
        verify(agendamentoService).criar(request.capture());
        assertEquals(11L, request.getValue().getClienteId());
    }

    @Test
    void clientCannotGeneratePixForAnotherClientsAppointment() throws Exception {
        when(agendamentoService.buscarPorId(41L)).thenReturn(
                AgendamentoResponseDTO.builder().id(41L).clienteId(22L).build());

        mockMvc.perform(post("/api/v1/agendamentos/41/pix")
                        .with(authenticatedClient(11L))
                        .contentType("application/json")
                        .content("{\"percentual\":30}"))
                .andExpect(status().isForbidden());

        verify(agendamentoService, never()).gerarPixSinal(eq(41L), any());
    }

    @Test
    void clientCannotMarkDepositOrFinalPaymentComplete() throws Exception {
        mockMvc.perform(patch("/api/v1/agendamentos/41/sinal-pago")
                        .with(authenticatedClient(11L)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/agendamentos/41/finalizar-pagamento")
                        .with(authenticatedClient(11L))
                        .contentType("application/json")
                        .content("{\"valorCobrancaAdicional\":0}"))
                .andExpect(status().isForbidden());

        verify(agendamentoService, never()).marcarSinalPago(41L);
        verify(agendamentoService, never()).finalizarPagamento(eq(41L), any());
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
