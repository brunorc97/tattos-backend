package com.tattos.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orcamentos")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id")
    private Projeto projeto;

    /** Valor fechado do trabalho (tipoCobranca = FECHADO). */
    @Column(name = "valor_total", precision = 10, scale = 2)
    private BigDecimal valorTotal;

    /** Valor cobrado por sessão (tipoCobranca = POR_SESSAO). */
    @Column(name = "valor_sessao", precision = 10, scale = 2)
    private BigDecimal valorSessao;

    @Column(name = "sessoes_min")
    private Integer sessoesMin;

    @Column(name = "sessoes_max")
    private Integer sessoesMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobranca", length = 20)
    private com.tattos.model.enums.TipoCobranca tipoCobranca;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private com.tattos.model.enums.StatusOrcamento status = com.tattos.model.enums.StatusOrcamento.RASCUNHO;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanbanCard> kanbanCards;
}
