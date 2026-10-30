package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class TrabalhoRequestDTO {

    @NotNull(message = "{validation.trabalho.tatuador.required}")
    @Schema(example = "1")
    private Long tatuadorId;

    @NotBlank(message = "{validation.trabalho.titulo.required}")
    @Size(max = 150)
    @Schema(example = "Dragão oriental nas costas")
    private String titulo;

    @Size(max = 100)
    @Schema(example = "Blackwork")
    private String estilo;

    @Size(max = 1000)
    @Schema(example = "Sessão única de 4h")
    private String descricao;

    @Size(max = 500)
    @Schema(example = "https://exemplo.com/foto.jpg")
    private String fotoUrl;
}
