package com.tattos.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;
import com.tattos.config.GoogleOAuthConfig;
import com.tattos.model.GoogleAccount;
import com.tattos.model.Usuario;
import com.tattos.repository.GoogleAccountRepository;
import com.tattos.security.JwtTokenProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleCalendarService {

    private final GoogleOAuthConfig googleOAuthConfig;
    private final GoogleAccountRepository googleAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private GoogleAuthorizationCodeFlow flow;

    @PostConstruct
    public void init() throws Exception {
        this.flow = googleOAuthConfig.googleAuthorizationCodeFlow();
    }

    /**
     * Generates the Google OAuth consent URL with signed, user-bound state.
     */
    public String getAuthorizationUrl(Long usuarioId) throws IOException {
        String redirectUri = "http://localhost:8080/api/google-calendar/callback"; // adjust as needed
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(jwtTokenProvider.generateGoogleCalendarState(usuarioId))
                .build();
    }

    /**
     * Handles the OAuth callback, exchanges the code for tokens and stores them.
     */
    public GoogleAccount handleCallback(Long usuarioId, String code) throws IOException {
        String redirectUri = "http://localhost:8080/api/google-calendar/callback";
        // Exchange authorization code for token response
        com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse tokenResponse =
                flow.newTokenRequest(code)
                        .setRedirectUri(redirectUri)
                        .execute();
        // Create credential from token response and store it for the user
        Credential credential = flow.createAndStoreCredential(tokenResponse, usuarioId.toString());
        // Use the token response to obtain a user identifier (email or sub). For simplicity we use the token's "email" field if present.
        String googleUserId = tokenResponse.get("email") != null ? tokenResponse.get("email").toString() : "user-" + usuarioId;
        GoogleAccount account = googleAccountRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> GoogleAccount.builder()
                        .usuario(new Usuario())
                        .googleUserId(googleUserId)
                        .build());
        account.setAccessToken(credential.getAccessToken());
        account.setRefreshToken(credential.getRefreshToken());
        if (credential.getExpirationTimeMilliseconds() != null) {
            account.setExpiryTime(Instant.ofEpochMilli(credential.getExpirationTimeMilliseconds())
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
        }
        account.setGoogleUserId(googleUserId);
        // associate user (only ID needed)
        Usuario u = new Usuario();
        u.setId(usuarioId);
        account.setUsuario(u);
        return googleAccountRepository.save(account);
    }

    /**
     * Retrieves upcoming events from the user's primary calendar.
     */
    public Events listUpcomingEvents(Long usuarioId) throws IOException {
        GoogleAccount account = googleAccountRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Google account not linked"));
        Credential credential = new Credential.Builder(
                flow.getMethod())
                .setTransport(flow.getTransport())
                .setJsonFactory(flow.getJsonFactory())
                .build()
                .setAccessToken(account.getAccessToken())
                .setRefreshToken(account.getRefreshToken())
                .setExpirationTimeMilliseconds(account.getExpiryTime() != null ?
                        account.getExpiryTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() : null);
        Calendar calendar = new Calendar.Builder(flow.getTransport(), flow.getJsonFactory(), credential)
                .setApplicationName("Tattos")
                .build();
        return calendar.events().list("primary")
                .setMaxResults(10)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
    }
}
