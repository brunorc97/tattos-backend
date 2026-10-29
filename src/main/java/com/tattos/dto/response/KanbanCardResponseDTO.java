package com.tattos.dto.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class KanbanCardResponseDTO {
    @Schema(example = "1")private Long id;
    private String titulo;
    private String descricao;
    private String status;
    private Integer ordem;
    private Long orcamentoId;
    private Long projetoId;
    private Boolean atrasado;
}
