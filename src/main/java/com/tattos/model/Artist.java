package com.tattos.model;

import com.tattos.model.enums.ArtistStatus;
import com.tattos.model.enums.ArtistTag;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtistTag tag; // RESIDENTE ou VISITANTE

    @Column(nullable = false)
    private String specialty;

    @Column(name = "split_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal splitRate; // % repassada ao artista (ex: 70.00)

    private String station;
    private String phone;
    private String instagram;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ArtistStatus status;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // Campos para Tatuador Visitante (Guest Spot)
    @Column(name = "origin_studio")
    private String originStudio;

    @Column(name = "guest_period_start")
    private LocalDate guestPeriodStart;

    @Column(name = "guest_period_end")
    private LocalDate guestPeriodEnd;

    @Builder.Default
    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions = 0;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
