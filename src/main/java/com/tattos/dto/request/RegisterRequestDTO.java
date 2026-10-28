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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {

    private String nome;
    private String name;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.invalid}")
    private String email;

    private String senha;
    private String password;

    private UserRole role;

    private String telefone;
    private String phone;

    private String endereco;
    private String address;

    private String especialidade;
    private String specialty;

    private String bio;

    private String cargo;

    private BigDecimal comissaoPercentual;

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
