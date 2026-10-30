package com.tattos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bancada")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bancada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da bancada é obrigatório")
    @Column(nullable = false, length = 50, unique = true)
    private String nome;

    @Column(length = 255)
    private String descricao;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativa = true;
}
