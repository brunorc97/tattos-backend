package com.tattos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "taxas_operadoras_parcelas",
       uniqueConstraints = @UniqueConstraint(columnNames = {"taxa_operadora_id", "parcelas"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxaOperadoraParcela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxa_operadora_id", nullable = false)
    private TaxaOperadora taxaOperadora;

    @Column(nullable = false)
    private Integer parcelas;

    @Column(name = "taxa_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaPercentual;
}
