package com.tattos.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoAgendamentoRequestDTO {

    @NotBlank(message = "{validation.required}")
    @Size(max = 5000)
    @Schema(example = "Dragão oriental cobrindo as costas")
    private String ideia;

    @Schema(example = "1")
    private Long imagemReferenciaId;

    @Schema(example = "3")
    private Long projetoId;

    @Schema(example = "2026-12-01")
    private LocalDate diaPreferencia;

    @Schema(example = "2026-W48")
    private String semanaPreferencia;

    @Schema(example = "2")
    private Long tatuadorId;

    @Schema(example = "Oriental")
    private String estilo;

    // ignorado no servidor: o clienteId vem sempre do token
    private Long clienteId;
}
