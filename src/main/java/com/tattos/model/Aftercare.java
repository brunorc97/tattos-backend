package com.tattos.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aftercare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Cliente related to the after‑care record.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * The agendamento (procedure) that this after‑care follows.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false, unique = true)
    private Agendamento agendamento;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Free‑form notes from the tattoo artist or client.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Healing progress percentage (0‑100). Updated by service based on days elapsed.
     */
    @Column(name = "healing_progress")
    private Integer healingProgress = 0;

    @PrePersist
    @PreUpdate
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates how many days have passed since the procedure date.
     */
    @Transient
    public long getDaysSinceProcedure() {
        if (agendamento == null || agendamento.getDataHora() == null) {
            return 0L;
        }
        return java.time.Duration.between(agendamento.getDataHora(), LocalDateTime.now()).toDays();
    }
}
