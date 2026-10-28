package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    
    @Schema(example = "gestor@tattos.com")private String email;
        @Schema(example = "gestor@tattos.com")private String username;

    
    @Schema(example = "senha123")private String senha;
        @Schema(example = "senha123")private String password;

    public String getLoginIdentifier() {
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        return "";
    }

    public String getRawPassword() {
        if (senha != null && !senha.isEmpty()) {
            return senha;
        }
        if (password != null && !password.isEmpty()) {
            return password;
        }
        return "";
    }
}
