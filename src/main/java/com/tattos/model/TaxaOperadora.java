package com.tattos.model;

import com.tattos.model.enums.ModalidadePagamento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "taxas_operadoras",
       uniqueConstraints = @UniqueConstraint(columnNames = {"operadora", "modalidade"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxaOperadora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String operadora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModalidadePagamento modalidade;

    @Column(name = "taxa_percentual", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxaPercentual;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    /** Grade de taxas por parcela (obrigatória quando modalidade = CREDITO_PARCELADO). */
    @Builder.Default
    @OneToMany(mappedBy = "taxaOperadora", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<TaxaOperadoraParcela> parcelasTaxas = new java.util.ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
