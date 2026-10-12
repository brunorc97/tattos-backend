package com.tattos.controller;

import com.tattos.model.Usuario;
import com.tattos.service.GoogleCalendarService;
import com.tattos.model.GoogleAccount;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    /**
     * Returns the Google OAuth consent URL for the authenticated user.
     */
    @GetMapping("/link")
    public ResponseEntity<String> getLink(@AuthenticationPrincipal Usuario usuario) throws Exception {
        String url = googleCalendarService.getAuthorizationUrl(usuario.getId());
        return ResponseEntity.ok(url);
    }

    /**
     * OAuth callback endpoint – receives "code" and "state" (user ID) from Google.
     */
    @PostMapping("/callback")
    public ResponseEntity<GoogleAccount> callback(
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank String state) throws Exception {
        Long userId = Long.valueOf(state);
        GoogleAccount account = googleCalendarService.handleCallback(userId, code);
        return ResponseEntity.ok(account);
    }

    /**
     * Lists upcoming events for the authenticated user.
     */
    @GetMapping("/events")
    public ResponseEntity<?> listEvents(@AuthenticationPrincipal Usuario usuario) throws Exception {
        return ResponseEntity.ok(googleCalendarService.listUpcomingEvents(usuario.getId()));
    }
}
