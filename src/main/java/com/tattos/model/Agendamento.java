package com.tattos.model;

import com.tattos.model.enums.StatusAgendamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data e hora do agendamento são obrigatórias")
    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Builder.Default
    @NotNull(message = "A duração em minutos é obrigatória")
    @Column(name = "duracao_minutos", nullable = false)
    private Integer duracaoMinutos = 60;

    @Column(name = "data_hora_fim", nullable = false)
    private LocalDateTime dataHoraFim;

    @NotBlank(message = "A identificação da cabine é obrigatória")
    @Column(nullable = false, length = 50)
    private String cabine;

    @Builder.Default
    @NotNull(message = "O status do agendamento é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Builder.Default
    @NotNull(message = "O valor do procedimento é obrigatório")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "valor_sinal", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorSinal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @NotNull(message = "O cliente é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @NotNull(message = "O tatuador é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Usuario tatuador;

    @OneToOne(mappedBy = "agendamento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FichaAnamnese fichaAnamnese;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void calcularHorarioFimEAtualizar() {
        if (this.dataHora != null) {
            int minutos = (this.duracaoMinutos != null && this.duracaoMinutos > 0) ? this.duracaoMinutos : 60;
            this.dataHoraFim = this.dataHora.plusMinutes(minutos);
        }
        this.updatedAt = LocalDateTime.now();
    }
}
