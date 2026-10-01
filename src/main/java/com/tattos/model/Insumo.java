package com.tattos.model;

import com.tattos.exception.BusinessException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, unique = true)
    private String codigo;

    @NotBlank(message = "O nome do insumo é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotNull(message = "A quantidade atual é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    @Column(name = "quantidade_atual", nullable = false)
    private Integer quantidadeAtual;

    @NotNull(message = "O nível crítico é obrigatório")
    @Min(value = 0, message = "O nível crítico não pode ser negativo")
    @Column(name = "nivel_critico", nullable = false)
    private Integer nivelCritico;

    @Builder.Default
    @NotBlank(message = "A unidade de medida é obrigatória")
    @Column(name = "unidade_medida", nullable = false, length = 30)
    private String unidadeMedida = "UN";

    @Column(length = 80)
    private String categoria;

    @Column(length = 50)
    private String lote;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Builder.Default
    @Column(name = "valor_unitario", precision = 10, scale = 2)
    private BigDecimal valorUnitario = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verifica se o insumo está em nível de estoque crítico ou esgotado.
     */
    @Transient
    public boolean isEstoqueCritico() {
        if (this.quantidadeAtual == null || this.nivelCritico == null) {
            return false;
        }
        return this.quantidadeAtual <= this.nivelCritico;
    }

    /**
     * Registra entrada de estoque.
     */
    public void adicionarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new BusinessException("A quantidade de entrada deve ser maior que zero.");
        }
        this.quantidadeAtual = (this.quantidadeAtual != null ? this.quantidadeAtual : 0) + quantidade;
    }

    /**
     * Registra saída / baixa de estoque com validação de saldo disponível.
     */
    public void removerEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new BusinessException("A quantidade de saída deve ser maior que zero.");
        }
        int atual = (this.quantidadeAtual != null ? this.quantidadeAtual : 0);
        if (atual < quantidade) {
            throw new BusinessException("Saldo insuficiente para o insumo '" + this.nome + "'. Disponível: " + atual + ", Solicitado: " + quantidade);
        }
        this.quantidadeAtual = atual - quantidade;
    }
}
