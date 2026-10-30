package com.tattos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaidaEstoqueDTO {

    @NotNull(message = "{validation.quantidade.required}")
    @Min(value = 1, message = "{validation.quantidade.min}")
    @Schema(example = "2")
    private Integer quantidade;

    /** Tatuador (ou gestor) que retirou o material. */
    @NotNull(message = "{validation.retiradoPor.required}")
    @Schema(example = "3")
    private Long retiradoPorId;

    @Schema(example = "Uso na sessão do cliente X")
    private String motivo;
}
