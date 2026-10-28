package com.tattos.model;

import com.tattos.model.enums.StatusAgendamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
@EntityListeners(AuditingEntityListener.class)
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

    @Builder.Default
    @Column(name = "desconto_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal descontoAplicado = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "lembrete_enviado", nullable = false)
    private boolean lembreteEnviado = false;

    @Builder.Default
    @Column(name = "sinal_pago", nullable = false)
    private boolean sinalPago = false;

    @Builder.Default
    @Column(name = "valor_cobranca_adicional", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorCobrancaAdicional = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "pagamento_completo", nullable = false)
    private boolean pagamentoCompleto = false;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "codigo_pix_sinal")
    private String codigoPixSinal;

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

    @CreatedBy
    @Column(name = "created_by", length = 150, updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by", length = 150)
    private String lastModifiedBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
