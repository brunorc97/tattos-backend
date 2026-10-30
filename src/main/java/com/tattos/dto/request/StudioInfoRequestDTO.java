package com.tattos.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/** Todos os campos opcionais — somente os informados são atualizados. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudioInfoRequestDTO {

    @Schema(example = "TattOS Studio")
    private String nome;

    @Schema(example = "Estúdio de tatuagem e body art.")
    private String descricao;

    @Schema(example = "Rua Exemplo, 123 - São Paulo/SP")
    private String endereco;

    @Schema(example = "Seg–Sáb, 09h às 19h")
    private String horario;

    @Schema(example = "@tattos.studio")
    private String instagram;
}
