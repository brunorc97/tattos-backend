package com.tattos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pagamentos_comissao_itens",
       uniqueConstraints = @UniqueConstraint(columnNames = "transacao_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoComissaoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id", nullable = false)
    private PagamentoComissao pagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id", nullable = false)
    private TransacaoFinanceira transacao;

    @Column(name = "valor_repasse", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorRepasse;
}
