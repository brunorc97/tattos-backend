package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BancadaRequestDTO {

    @NotBlank(message = "{validation.bancada.nome.required}")
    @Schema(example = "Bancada 1")
    private String nome;

    @Schema(example = "Perto da janela")
    private String descricao;

    @Schema(example = "true")
    private Boolean ativa;
}
