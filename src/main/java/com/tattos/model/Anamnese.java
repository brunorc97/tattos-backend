package com.tattos.model;

import com.tattos.model.enums.AnamneseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "anamneses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // ex: ANAM-2026-001

    @Column(name = "client_name", nullable = false, length = 150)
    private String clientName;

    @Column(nullable = false, length = 20)
    private String cpf;

    @Column(length = 30)
    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "procedure_type", length = 50)
    private String procedureType; // Tatuagem, Body Piercing

    @Column(name = "body_location", length = 150)
    private String bodyLocation;

    @Column(name = "artist_name", length = 150)
    private String artistName;

    @Column(name = "anamnese_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AnamneseStatus status;

    @Builder.Default
    @Column(name = "has_medical_alert", nullable = false)
    private Boolean hasMedicalAlert = false;

    @Column(columnDefinition = "TEXT")
    private String allergies;

    // Assinatura digital em Base64
    @Lob
    @Column(name = "signature_data", columnDefinition = "LONGTEXT")
    private String signatureData;

    // Questionário de Saúde em booleanos
    @Builder.Default
    @Column(nullable = false)
    private Boolean diabetes = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean hemofilia = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean keloids = false;

    @Builder.Default
    @Column(name = "hepatite_hiv", nullable = false)
    private Boolean hepatiteHiv = false;

    @Builder.Default
    @Column(name = "pregnant_nursing", nullable = false)
    private Boolean pregnantNursing = false;

    @Builder.Default
    @Column(name = "alcohol_24h", nullable = false)
    private Boolean alcohol24h = false;

    @Builder.Default
    @Column(name = "fed_well", nullable = false)
    private Boolean fedWell = true;

    @Builder.Default
    @Column(name = "cardiac_condition", nullable = false)
    private Boolean cardiacCondition = false;

    @Builder.Default
    @Column(name = "medications_in_use", nullable = false)
    private Boolean medicationsInUse = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
