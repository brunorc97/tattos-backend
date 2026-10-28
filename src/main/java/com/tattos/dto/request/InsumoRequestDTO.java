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

    @NotBlank(message = "{validation.insumo.nome.required}")
    private String nome;

    @NotNull(message = "{validation.quantidade.required}")
    @Min(value = 0, message = "{validation.quantidade.nonnegative}")
    private Integer quantidadeAtual;

    @NotNull(message = "{validation.nivel.critico.required}")
    @Min(value = 0, message = "{validation.nivel.critico.nonnegative}")
    private Integer nivelCritico;

    @Builder.Default
    private String unidadeMedida = "UN";

    private String categoria;

    private String lote;

    private LocalDate dataValidade;

    private BigDecimal valorUnitario;
}
