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

/**
 * Trabalho do portfólio da vitrine do estúdio. Pode ser uma foto solta de
 * portfólio (sem cliente) ou vinculado a uma sessão real (cliente/trabalho
 * realizado) — somente os vinculados são avaliáveis.
 */
@Entity
@Table(name = "trabalho")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Usuario tatuador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabalho_realizado_id")
    private TrabalhoRealizado trabalhoRealizado;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 100)
    private String estilo;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "foto_url", length = 500)
    private String fotoUrl;

    @Column(nullable = false)
    private LocalDate data;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
