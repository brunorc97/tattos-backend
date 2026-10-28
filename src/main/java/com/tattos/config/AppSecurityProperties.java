package com.tattos.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configurações de segurança da aplicação TattOS.
 *
 * <p>Todos os valores sensíveis DEVEM ser fornecidos via variáveis de ambiente.
 * Não há valores padrão (fallback) para evitar vazamento de credenciais em produção.</p>
 *
 * <p>Variáveis de ambiente necessárias:</p>
 * <ul>
 *   <li>{@code JWT_SECRET} – segredo para assinatura de tokens JWT (mínimo 32 caracteres)</li>
 *   <li>{@code DB_USERNAME} – usuário do banco de dados</li>
 *   <li>{@code DB_PASSWORD} – senha do banco de dados</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.security")
@Validated
@Getter
@Setter
public class AppSecurityProperties {

    /**
     * Segredo usado para assinar os tokens JWT.
     * Deve ter no mínimo 32 caracteres para garantir segurança adequada com HMAC-SHA256.
     * Configure via: APP_SECURITY_JWT_SECRET (ou jwt.secret no application.yml via ${JWT_SECRET})
     */
    @NotBlank(message = "app.security.jwt-secret não pode ser vazio — configure a variável JWT_SECRET")
    @Size(min = 32, message = "O JWT secret deve ter no mínimo 32 caracteres para segurança adequada")
    private String jwtSecret = "";

    /**
     * Tempo de expiração do token JWT em milissegundos.
     * Padrão: 86400000 ms (24 horas).
     */
    @Min(value = 300000, message = "A expiração do JWT deve ser de pelo menos 5 minutos (300000 ms)")
    private long jwtExpiration = 86400000L;
}
