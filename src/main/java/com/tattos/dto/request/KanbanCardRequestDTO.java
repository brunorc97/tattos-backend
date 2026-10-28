package com.tattos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KanbanCardRequestDTO {

    @NotBlank(message = "{validation.kanban.titulo.required}")
    @Size(max = 255, message = "{validation.kanban.titulo.size}")
    private String titulo;

    private String descricao;

    @NotBlank(message = "{validation.kanban.status.required}")
    @Size(max = 50, message = "{validation.kanban.status.size}")
    private String status;

    @NotNull(message = "{validation.kanban.ordem.required}")
    private Integer ordem;

    private Long orcamentoId;

    private Long projetoId;
}
