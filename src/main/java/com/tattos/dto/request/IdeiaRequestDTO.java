package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class IdeiaRequestDTO {

    @NotBlank(message = "{validation.ideia.titulo.required}")
    @Size(max = 150)
    @Schema(example = "Dragão minimalista")
    private String titulo;

    @Size(max = 100)
    @Schema(example = "Fine Line")
    private String estilo;

    @Size(max = 1000)
    @Schema(example = "Traços finos, antebraço")
    private String descricao;

    @Size(max = 500)
    @Schema(example = "https://exemplo.com/ideia.jpg")
    private String imagemUrl;
}
