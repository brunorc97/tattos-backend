package com.tattos.model;

import com.tattos.model.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Histórico de entrada/saída de estoque. Entrada pode registrar o custo
 * unitário do material (base do custo médio simples); saída registra quem
 * retirou (tatuador ou gestor).
 */
@Entity
@Table(name = "movimentacao_estoque")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    @Column(nullable = false)
    private Integer quantidade;

    /** Custo unitário do material na entrada (null quando não informado). */
    @Column(name = "custo_unitario", precision = 10, scale = 2)
    private BigDecimal custoUnitario;

    /** Quem retirou o material (saída); null nas entradas. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "retirado_por_id")
    private Usuario retiradoPor;

    @Column(length = 255)
    private String motivo;

    @CreatedBy
    @Column(name = "created_by", length = 150, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
