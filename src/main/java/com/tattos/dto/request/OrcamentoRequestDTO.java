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

    @NotBlank(message = "O nome do orçamento é obrigatório")
    @Size(min = 2, max = 255, message = "O nome do orçamento deve ter entre 2 e 255 caracteres")
    private String nome;

    @NotNull(message = "O valor do orçamento é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor do orçamento deve ser maior que zero")
    private BigDecimal valor;

    private Long projetoId;
}
