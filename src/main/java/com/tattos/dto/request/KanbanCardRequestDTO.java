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
public class KanbanCardRequestDTO {

    @NotBlank(message = "{validation.kanban.titulo.required}")
    @Size(max = 255, message = "{validation.kanban.titulo.size}")
        @Schema(example = "Orçamento - Fechamento de braço")private String titulo;

    
    @Schema(example = "Cliente Maria, referência floral")private String descricao;

    @NotBlank(message = "{validation.kanban.status.required}")
    @Size(max = 50, message = "{validation.kanban.status.size}")
        @Schema(example = "A_FAZER")private String status;

    @NotNull(message = "{validation.kanban.ordem.required}")
        @Schema(example = "1")private Integer ordem;

    
    @Schema(example = "5")private Long orcamentoId;

    
    @Schema(example = "3")private Long projetoId;
}
