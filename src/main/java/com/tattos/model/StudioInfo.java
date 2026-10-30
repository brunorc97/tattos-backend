package com.tattos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Informações públicas do estúdio (registro único). */
@Entity
@Table(name = "studio_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudioInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Column(length = 255)
    private String endereco;

    @Column(length = 150)
    private String horario;

    @Column(length = 150)
    private String instagram;
}
