package com.tattos.model;

import com.tattos.model.enums.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ROLE_CLIENTE")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Cliente extends Usuario {

    public Cliente(String nome, String email, String senha, String telefone, String endereco) {
        this();
        setNome(nome);
        setEmail(email);
        setSenha(senha);
        setTelefone(telefone);
        setEndereco(endereco);
    }

    {
        this.role = UserRole.ROLE_CLIENTE;
    }
}
