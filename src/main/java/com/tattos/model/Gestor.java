package com.tattos.model;

import com.tattos.model.enums.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ROLE_GESTOR")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Gestor extends Usuario {

    public Gestor(String nome, String email, String senha, String cargo) {
        this();
        setNome(nome);
        setEmail(email);
        setSenha(senha);
        setCargo(cargo);
    }

    {
        this.role = UserRole.ROLE_GESTOR;
    }
}
