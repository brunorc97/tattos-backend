package com.tattos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cupons")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupom {

    public enum Tipo { PORCENTAGEM, VALOR_FIXO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /** null = vale para todo o studio; preenchido = só para este tatuador. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id")
    private Usuario tatuador;

    private LocalDate validade;

    @Column(name = "limite_usos")
    private Integer limiteUsos;

    @Builder.Default
    @Column(nullable = false)
    private Integer usos = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por")
    private Usuario criadoPor;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
