package com.tattos.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projetos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projeto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tatuador_id")
    private Usuario tatuador;

    @Column
    private java.time.LocalDate prazo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status_aprovacao", nullable = false, length = 20)
    private com.tattos.model.enums.StatusAprovacaoProjeto statusAprovacao =
            com.tattos.model.enums.StatusAprovacaoProjeto.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_recusa", length = 30)
    private com.tattos.model.enums.MotivoRecusaProjeto motivoRecusa;

    @Column(name = "motivo_recusa_detalhe", length = 500)
    private String motivoRecusaDetalhe;

    @Builder.Default
    @Column(name = "necessita_sinal", nullable = false)
    private boolean necessitaSinal = false;

    @Column(name = "valor_sinal", precision = 10, scale = 2)
    private java.math.BigDecimal valorSinal;

    @Builder.Default
    @Column(name = "sinal_pago", nullable = false)
    private boolean sinalPago = false;

    @Column(name = "data_pagamento_sinal")
    private LocalDateTime dataPagamentoSinal;

    @Column(name = "forma_pagamento_sinal", length = 30)
    private String formaPagamentoSinal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprovante_sinal_id")
    private Arquivo comprovanteSinal;

    /** Compatibilidade com o modelo legado (boolean). */
    public boolean isAprovado() {
        return statusAprovacao == com.tattos.model.enums.StatusAprovacaoProjeto.APROVADO;
    }

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orcamento> orcamentos;

    @OneToMany(mappedBy = "projeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanbanCard> kanbanCards;
}
