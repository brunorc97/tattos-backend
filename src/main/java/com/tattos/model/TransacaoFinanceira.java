package com.tattos.model;

import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacoes_financeiras")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição da transação é obrigatória")
    @Column(nullable = false, length = 255)
    private String descricao;

    @NotNull(message = "O valor da transação é obrigatório")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @NotNull(message = "O tipo da transação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipo;

    @NotNull(message = "A data da transação é obrigatória")
    @Column(nullable = false)
    private LocalDate data;

    @Builder.Default
    @NotNull(message = "O status da transação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTransacao status = StatusTransacao.CONCLUIDO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id")
    private Usuario tatuador;

    @Column(length = 80)
    private String categoria;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxa_operadora_id")
    private TaxaOperadora taxaOperadora;

    @Builder.Default
    @Column(name = "taxa_valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaValor = BigDecimal.ZERO;

    @Column(name = "valor_liquido", precision = 10, scale = 2)
    private BigDecimal valorLiquido;

    /** Adiantamentos: quanto já foi abatido em pagamentos de comissão (FIFO). */
    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal abatido = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_id")
    private Arquivo recibo;

    /** Status do repasse ao tatuador (pagamento de comissão). */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status_repasse", nullable = false, length = 20)
    private com.tattos.model.enums.StatusRepasse statusRepasse = com.tattos.model.enums.StatusRepasse.PENDENTE;

    @Builder.Default
    @Column(name = "comissao_percentual", precision = 5, scale = 2)
    private BigDecimal comissaoPercentual = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "valor_repasse", precision = 10, scale = 2)
    private BigDecimal valorRepasse = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id")
    private Agendamento agendamento;

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
    public void calcularRepasse() {
        // valor líquido sempre definido (receitas sem taxa: líquido = valor)
        if (this.valor != null && this.valorLiquido == null) {
            this.valorLiquido = this.valor.subtract(this.taxaValor != null ? this.taxaValor : BigDecimal.ZERO);
        }
        if (TipoTransacao.RECEITA.equals(this.tipo) && this.tatuador != null && this.valor != null) {
            BigDecimal taxa = (this.comissaoPercentual != null && this.comissaoPercentual.compareTo(BigDecimal.ZERO) > 0)
                    ? this.comissaoPercentual
                    : (this.tatuador.getComissaoPercentual() != null ? this.tatuador.getComissaoPercentual() : new BigDecimal("70.00"));
            this.comissaoPercentual = taxa;
            this.valorRepasse = this.valor.multiply(taxa).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        } else if (TipoTransacao.DESPESA.equals(this.tipo)) {
            this.comissaoPercentual = BigDecimal.ZERO;
            this.valorRepasse = BigDecimal.ZERO;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
