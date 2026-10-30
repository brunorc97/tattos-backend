package com.tattos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntradaEstoqueDTO {

    @NotNull(message = "{validation.quantidade.required}")
    @Min(value = 1, message = "{validation.quantidade.min}")
    @Schema(example = "10")
    private Integer quantidade;

    /** Custo unitário do material nesta entrada; entra no custo médio simples. */
    @DecimalMin(value = "0.00", message = "{validation.custo.nonnegative}")
    @Schema(example = "2.50")
    private BigDecimal custoUnitario;

    @Schema(example = "Reposição semanal")
    private String motivo;
}
