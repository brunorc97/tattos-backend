package com.tattos.model;

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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pagamentos_comissao")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoComissao {

    public enum Status { AGENDADO, PAGO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id", nullable = false)
    private Usuario tatuador;

    @Column(name = "total_bruto", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalBruto;

    @Builder.Default
    @Column(name = "total_adiantamentos_abatidos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAdiantamentosAbatidos = BigDecimal.ZERO;

    @Column(name = "total_liquido", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalLiquido;

    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private String formaPagamento;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.AGENDADO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprovante_id")
    private Arquivo comprovante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recibo_arquivo_id")
    private Arquivo reciboArquivo;

    @Builder.Default
    @OneToMany(mappedBy = "pagamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PagamentoComissaoItem> itens = new ArrayList<>();

    @CreatedBy
    @Column(name = "created_by", length = 150, updatable = false)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "pago_em")
    private LocalDateTime pagoEm;

    @Column(name = "periodo_inicio")
    private java.time.LocalDate periodoInicio;

    @Column(name = "periodo_fim")
    private java.time.LocalDate periodoFim;
}
