package com.tattos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsumoRequestDTO {

    private String codigo;

    @NotBlank(message = "O nome do insumo é obrigatório")
    private String nome;

    @NotNull(message = "A quantidade inicial é obrigatória")
    @Min(value = 0, message = "A quantidade inicial não pode ser negativa")
    private Integer quantidadeAtual;

    @NotNull(message = "O nível crítico é obrigatório")
    @Min(value = 0, message = "O nível crítico não pode ser negativo")
    private Integer nivelCritico;

    @Builder.Default
    private String unidadeMedida = "UN";

    private String categoria;

    private String lote;

    private LocalDate dataValidade;

    private BigDecimal valorUnitario;
}
