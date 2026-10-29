package com.tattos.security;

import com.tattos.config.AppSecurityProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        AppSecurityProperties properties = new AppSecurityProperties();
        properties.setJwtSecret("test-secret-key-with-at-least-thirty-two-characters");
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    void googleCalendarStateIsSignedAndBoundToItsUser() {
        String state = jwtTokenProvider.generateGoogleCalendarState(11L);

        assertEquals(11L, jwtTokenProvider.getGoogleCalendarUserIdFromState(state));
    }

    @Test
    void googleCalendarStateRejectsTampering() {
        String state = jwtTokenProvider.generateGoogleCalendarState(11L);
        String[] parts = state.split("\\.");
        parts[2] = (parts[2].startsWith("a") ? "b" : "a") + parts[2].substring(1);
        String tamperedState = String.join(".", parts);

        assertThrows(JwtException.class, () -> jwtTokenProvider.getGoogleCalendarUserIdFromState(tamperedState));
    }
}
