package com.tattos.dto.request;

import com.tattos.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    
    @Schema(example = "Ana Souza")private String nome;
        @Schema(example = "Ana Souza")private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
        @Schema(example = "ana@exemplo.com")private String email;

    
    @Schema(example = "senha123")private String senha;
        @Schema(example = "senha123")private String password;

    
    @Schema(example = "ROLE_CLIENTE")private UserRole role;

    
    @Schema(example = "(11) 99999-0000")private String telefone;
        @Schema(example = "(11) 99999-0000")private String phone;

    
    @Schema(example = "Rua Augusta, 1000")private String endereco;
        @Schema(example = "Rua Augusta, 1000")private String address;

    
    @Schema(example = "Fineline")private String especialidade;
        @Schema(example = "Fineline")private String specialty;

    
    @Schema(example = "Tatuadora há 8 anos")private String bio;

    
    @Schema(example = "Recepcionista")private String cargo;

    
    @Schema(example = "60.00")private BigDecimal comissaoPercentual;

    public String getResolvedNome() {
        if (nome != null && !nome.trim().isEmpty()) return nome.trim();
        if (name != null && !name.trim().isEmpty()) return name.trim();
        return "";
    }

    public String getResolvedSenha() {
        if (senha != null && !senha.isEmpty()) return senha;
        if (password != null && !password.isEmpty()) return password;
        return "";
    }

    public String getResolvedTelefone() {
        if (telefone != null && !telefone.trim().isEmpty()) return telefone.trim();
        if (phone != null && !phone.trim().isEmpty()) return phone.trim();
        return null;
    }

    public String getResolvedEndereco() {
        if (endereco != null && !endereco.trim().isEmpty()) return endereco.trim();
        if (address != null && !address.trim().isEmpty()) return address.trim();
        return null;
    }

    public String getResolvedEspecialidade() {
        if (especialidade != null && !especialidade.trim().isEmpty()) return especialidade.trim();
        if (specialty != null && !specialty.trim().isEmpty()) return specialty.trim();
        return null;
    }
}
