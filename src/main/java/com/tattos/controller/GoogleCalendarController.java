package com.tattos.controller;

import com.tattos.model.Usuario;
import com.tattos.security.JwtTokenProvider;
import com.tattos.service.GoogleCalendarService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping({"/api/v1/google-calendar", "/api/google-calendar"})
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Returns the Google OAuth consent URL for the authenticated user.
     */
    @GetMapping("/link")
    public ResponseEntity<String> getLink(@AuthenticationPrincipal Usuario usuario) throws Exception {
        String url = googleCalendarService.getAuthorizationUrl(usuario.getId());
        return ResponseEntity.ok(url);
    }

    /**
     * Redirects Google's OAuth callback to the frontend for authenticated code exchange.
     */
    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank String state) throws Exception {
        URI location = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/integracoes")
                .queryParam("oauth_code", code)
                .queryParam("state", state)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @PostMapping("/callback")
    public ResponseEntity<Void> exchangeCallback(
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank String state,
            @AuthenticationPrincipal Usuario usuario) throws Exception {
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long stateUserId;
        try {
            stateUserId = jwtTokenProvider.getGoogleCalendarUserIdFromState(state);
        } catch (JwtException | IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
        if (!stateUserId.equals(usuario.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        googleCalendarService.handleCallback(stateUserId, code);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists upcoming events for the authenticated user.
     */
    @GetMapping("/events")
    public ResponseEntity<?> listEvents(@AuthenticationPrincipal Usuario usuario) throws Exception {
        return ResponseEntity.ok(googleCalendarService.listUpcomingEvents(usuario.getId()));
    }
}
