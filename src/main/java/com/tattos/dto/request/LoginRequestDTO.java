package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO {

    private String email;
    private String username;

    private String senha;
    private String password;

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
