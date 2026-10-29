package com.tattos.controller;

import com.tattos.model.Cliente;
import com.tattos.model.Usuario;
import com.tattos.security.JwtTokenProvider;
import com.tattos.service.GoogleCalendarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private GoogleCalendarService googleCalendarService;

    @Test
    void oauthRedirectCallbackForwardsSignedStateToFrontend() throws Exception {
        String state = jwtTokenProvider.generateGoogleCalendarState(11L);

        mockMvc.perform(get("/api/v1/google-calendar/callback")
                        .param("code", "oauth-code")
                        .param("state", state))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "http://localhost:3000/integracoes?oauth_code=oauth-code&state=" + state));

        verify(googleCalendarService, never()).handleCallback(11L, "oauth-code");
    }

    @Test
    void authenticatedCallbackBindsGoogleCodeToTheSignedInUser() throws Exception {
        String state = jwtTokenProvider.generateGoogleCalendarState(11L);

        mockMvc.perform(post("/api/v1/google-calendar/callback")
                        .with(authenticatedClient(11L))
                        .param("code", "oauth-code")
                        .param("state", state))
                .andExpect(status().isNoContent());

        verify(googleCalendarService).handleCallback(11L, "oauth-code");
    }

    @Test
    void authenticatedCallbackRejectsStateForAnotherUser() throws Exception {
        String state = jwtTokenProvider.generateGoogleCalendarState(22L);

        mockMvc.perform(post("/api/v1/google-calendar/callback")
                        .with(authenticatedClient(11L))
                        .param("code", "oauth-code")
                        .param("state", state))
                .andExpect(status().isForbidden());

        verify(googleCalendarService, never()).handleCallback(22L, "oauth-code");
    }

    @Test
    void calendarLinkAndEventsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/google-calendar/link"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/google-calendar/events"))
                .andExpect(status().isUnauthorized());
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
