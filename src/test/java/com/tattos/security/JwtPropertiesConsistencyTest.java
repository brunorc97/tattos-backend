package com.tattos.security;

import com.tattos.config.AppSecurityProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Garante que o JwtTokenProvider é construído exclusivamente a partir de
 * AppSecurityProperties (namespace único app.security), eliminando a
 * duplicidade jwt.secret vs app.security.jwt-secret.
 */
class JwtPropertiesConsistencyTest {

    @Test
    @DisplayName("Provider construído com AppSecurityProperties gera e valida tokens")
    void providerUsaAppSecurityProperties() {
        AppSecurityProperties props = new AppSecurityProperties();
        props.setJwtSecret("segredo-de-teste-com-mais-de-32-caracteres!!");
        props.setJwtExpiration(3_600_000L);

        JwtTokenProvider provider = new JwtTokenProvider(props);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user@exemplo.com", null, List.of(new SimpleGrantedAuthority("ROLE_GESTOR")));

        String token = provider.generateToken(auth);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsernameFromToken(token)).isEqualTo("user@exemplo.com");
        assertThat(provider.getExpirationTime()).isEqualTo(3_600_000L);
    }

    @Test
    @DisplayName("Segredos diferentes entre emissor e validador devem rejeitar o token")
    void segredosDivergentesRejeitamToken() {
        AppSecurityProperties emissor = new AppSecurityProperties();
        emissor.setJwtSecret("segredo-A-de-teste-com-mais-de-32-caracteres");

        AppSecurityProperties validador = new AppSecurityProperties();
        validador.setJwtSecret("segredo-B-de-teste-com-mais-de-32-caracteres");

        String token = new JwtTokenProvider(emissor).generateToken(
                new UsernamePasswordAuthenticationToken("user@exemplo.com", null, List.of()));

        assertThat(new JwtTokenProvider(validador).validateToken(token)).isFalse();
    }
}
