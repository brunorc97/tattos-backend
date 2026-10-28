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
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsumoRequestDTO {

    
    @Schema(example = "AGU-3RL")private String codigo;

    @NotBlank(message = "{validation.insumo.nome.required}")
        @Schema(example = "Agulha 3RL")private String nome;

    @NotNull(message = "{validation.quantidade.required}")
    @Min(value = 0, message = "{validation.quantidade.nonnegative}")
        @Schema(example = "100")private Integer quantidadeAtual;

    @NotNull(message = "{validation.nivel.critico.required}")
    @Min(value = 0, message = "{validation.nivel.critico.nonnegative}")
        @Schema(example = "20")private Integer nivelCritico;

    @Builder.Default
        @Schema(example = "UN")private String unidadeMedida = "UN";

    
    @Schema(example = "Agulhas")private String categoria;

    
    @Schema(example = "L2026-01")private String lote;

    
    @Schema(example = "2027-06-30")private LocalDate dataValidade;

    
    @Schema(example = "2.50")private BigDecimal valorUnitario;
}
