package com.tattos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fichas_anamnese")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichaAnamnese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento;

    @Column(name = "historico_saude", columnDefinition = "TEXT")
    private String historicoSaude;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Builder.Default
    @Column(name = "termo_assinado", nullable = false)
    private Boolean termoAssinado = false;

    @Lob
    @Column(name = "assinatura_digital", columnDefinition = "LONGTEXT")
    private String assinaturaDigital;

    @Builder.Default
    @Column(name = "alerta_clinico", nullable = false)
    private Boolean alertaClinico = false;

    @Builder.Default
    @Column(name = "risco_dermatologico", nullable = false)
    private Boolean riscoDermatologico = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean diabetes = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean hemofilia = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean hipertensao = false;

    @Builder.Default
    @Column(name = "gestante_lactante", nullable = false)
    private Boolean gestanteLactante = false;

    @Column(name = "medicamentos_uso", columnDefinition = "TEXT")
    private String medicamentosUso;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Builder.Default
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    /**
     * Avalia os alertas clínicos automaticamente com base nas condições informadas.
     */
    public void avaliarAlertasClinicos() {
        if (Boolean.TRUE.equals(this.diabetes) ||
            Boolean.TRUE.equals(this.hemofilia) ||
            Boolean.TRUE.equals(this.hipertensao) ||
            Boolean.TRUE.equals(this.gestanteLactante) ||
            (this.alergias != null && !this.alergias.trim().isEmpty())) {
            this.alertaClinico = true;
        }

        if (Boolean.TRUE.equals(this.diabetes) ||
            (this.historicoSaude != null && this.historicoSaude.toLowerCase().contains("queloide")) ||
            (this.alergias != null && (this.alergias.toLowerCase().contains("latex") || this.alergias.toLowerCase().contains("pigmento")))) {
            this.riscoDermatologico = true;
        }
    }
}
