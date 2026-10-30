package com.tattos.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvaliacaoRequestDTO {

    @NotNull(message = "{validation.avaliacao.trabalho.required}")
    @Schema(example = "101")
    private Long trabalhoId;

    @NotNull(message = "{validation.avaliacao.nota.required}")
    @Min(value = 1, message = "{validation.avaliacao.nota.range}")
    @Max(value = 5, message = "{validation.avaliacao.nota.range}")
    @Schema(example = "5")
    private Integer notaTatuagem;

    @NotNull(message = "{validation.avaliacao.nota.required}")
    @Min(value = 1, message = "{validation.avaliacao.nota.range}")
    @Max(value = 5, message = "{validation.avaliacao.nota.range}")
    @Schema(example = "4")
    private Integer notaTatuador;

    @Size(max = 1000)
    @Schema(example = "Ficou perfeita!")
    private String comentario;
}
