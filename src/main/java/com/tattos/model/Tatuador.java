package com.tattos.model;

import com.tattos.model.enums.UserRole;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ROLE_TATUADOR")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Tatuador extends Usuario {

    public Tatuador(String nome, String email, String senha, String especialidade, String bio) {
        this();
        setNome(nome);
        setEmail(email);
        setSenha(senha);
        setEspecialidade(especialidade);
        setBio(bio);
    }

    {
        this.role = UserRole.ROLE_TATUADOR;
    }
}
