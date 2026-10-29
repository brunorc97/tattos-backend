package com.tattos.security;

import com.tattos.config.AppSecurityProperties;
import com.tattos.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final long GOOGLE_CALENDAR_STATE_TTL_MS = 5 * 60 * 1000L;
    private static final String GOOGLE_CALENDAR_STATE_PURPOSE = "google-calendar-oauth";

    private final SecretKey key;
    private final long expirationTime;

    public JwtTokenProvider(AppSecurityProperties properties) {
        byte[] keyBytes = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expirationTime = properties.getJwtExpiration();
    }

    public long getExpirationTime() {
        return this.expirationTime;
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        var builder = Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate);

        if (authentication.getPrincipal() instanceof Usuario usuario) {
            builder.claim("userId", usuario.getId())
                   .claim("name", usuario.getNome());
        }

        return builder.signWith(key).compact();
    }

    public String generateTokenForUser(Usuario usuario) {
        String roles = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("userId", usuario.getId())
                .claim("name", usuario.getNome())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String generateGoogleCalendarState(Long usuarioId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(GOOGLE_CALENDAR_STATE_PURPOSE)
                .claim("purpose", GOOGLE_CALENDAR_STATE_PURPOSE)
                .claim("userId", usuarioId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + GOOGLE_CALENDAR_STATE_TTL_MS))
                .signWith(key)
                .compact();
    }

    public Long getGoogleCalendarUserIdFromState(String state) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(state)
                .getPayload();

        if (!GOOGLE_CALENDAR_STATE_PURPOSE.equals(claims.get("purpose", String.class))) {
            throw new JwtException("Invalid Google Calendar OAuth state");
        }

        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        throw new JwtException("Invalid Google Calendar OAuth state");
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
