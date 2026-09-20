package com.tattos.dto.request;

import com.tattos.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 80, message = "O nome de usuário deve ter entre 3 e 80 caracteres")
    String username,

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    String password,

    @NotBlank(message = "O nome completo é obrigatório")
    String name,

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Formato de e-mail inválido")
    String email,

    @NotNull(message = "O perfil de acesso (Role) é obrigatório")
    UserRole role
) {}
