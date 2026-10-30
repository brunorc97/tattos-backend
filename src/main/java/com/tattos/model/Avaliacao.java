package com.tattos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Avaliação de um trabalho pelo cliente dono — uma por trabalho. */
@Entity
@Table(name = "avaliacao")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabalho_id", nullable = false, unique = true)
    private Trabalho trabalho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Usuario tatuador;

    @Column(name = "nota_tatuagem", nullable = false)
    private Integer notaTatuagem;

    @Column(name = "nota_tatuador", nullable = false)
    private Integer notaTatuador;

    @Column(length = 1000)
    private String comentario;

    @Column(nullable = false)
    private LocalDate data;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
