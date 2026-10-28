package com.tattos.config;

import com.tattos.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.disable())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/v1/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/google-calendar/**", "/api/v1/google-calendar/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Módulo Financeiro e Comissões
                .requestMatchers("/api/financeiro/**", "/api/financial/**", "/api/comissoes/**", "/api/commissions/**",
                        "/api/v1/financeiro/**", "/api/v1/financial/**", "/api/v1/comissoes/**", "/api/v1/commissions/**").hasAnyRole("GESTOR", "TATUADOR")

                // Módulo de Estoque e Insumos
                .requestMatchers("/api/estoque/**", "/api/stock/**", "/api/insumos/**",
                        "/api/v1/estoque/**", "/api/v1/stock/**", "/api/v1/insumos/**").hasAnyRole("GESTOR", "TATUADOR")

                // Módulo de Fichas de Anamnese
                .requestMatchers("/api/fichas-anamnese/**", "/api/anamnese/**",
                        "/api/v1/fichas-anamnese/**", "/api/v1/anamnese/**", "/api/v1/anamneses/**").hasAnyRole("GESTOR", "TATUADOR", "CLIENTE")

                // Módulo de Agendamentos
                .requestMatchers("/api/agendamentos/**", "/api/appointments/**", "/api/sessions/**",
                        "/api/v1/agendamentos/**", "/api/v1/appointments/**", "/api/v1/sessions/**").hasAnyRole("GESTOR", "TATUADOR", "CLIENTE")

                // Módulos Operacionais: Projetos, Orçamentos, Kanban e Aftercare
                .requestMatchers("/api/projetos/**", "/api/orcamentos/**", "/api/kanban-cards/**", "/api/aftercare/**",
                        "/api/v1/projetos/**", "/api/v1/projects/**", "/api/v1/orcamentos/**", "/api/v1/budgets/**",
                        "/api/v1/kanban/**", "/api/v1/kanban-cards/**", "/api/v1/aftercare/**").hasAnyRole("GESTOR", "TATUADOR")

                // Módulo de Usuários
                .requestMatchers("/api/usuarios/**", "/api/artists/**", "/api/tatuadores/**",
                        "/api/v1/usuarios/**", "/api/v1/artists/**", "/api/v1/tatuadores/**").authenticated()

                // Qualquer outra rota exige autenticação
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "http://localhost:5173", "https://tattos-frontend.example.com"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
