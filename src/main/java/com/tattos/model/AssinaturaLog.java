package com.tattos.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de quando um termo de consentimento (assinatura digital) foi confirmado.
 * Armazena referência à ficha de anamnese, data/hora, e‑mail utilizado para envio ao cliente e, opcionalmente, o usuário que realizou a ação.
 */
@Entity
@Table(name = "assinatura_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssinaturaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ficha_anamnese_id", nullable = false)
    private FichaAnamnese fichaAnamnese;

    @Column(name = "data_assinatura", nullable = false)
    private LocalDateTime dataAssinatura;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // opcional, quem assinou no sistema
}
