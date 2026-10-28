package com.tattos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrcamentoRequestDTO {

    @NotBlank(message = "{validation.orcamento.nome.required}")
    @Size(min = 2, max = 255, message = "{validation.orcamento.nome.size}")
    private String nome;

    @NotNull(message = "{validation.valor.required}")
    @DecimalMin(value = "0.01", message = "{validation.positive}")
    private BigDecimal valor;

    private Long projetoId;
}
