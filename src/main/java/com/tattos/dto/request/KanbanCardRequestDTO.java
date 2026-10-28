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

    @NotBlank(message = "O título do card é obrigatório")
    @Size(max = 255, message = "O título deve ter no máximo 255 caracteres")
    private String titulo;

    private String descricao;

    @NotBlank(message = "O status do card é obrigatório")
    @Size(max = 50, message = "O status deve ter no máximo 50 caracteres")
    private String status;

    @NotNull(message = "A ordem do card é obrigatória")
    private Integer ordem;

    private Long orcamentoId;

    private Long projetoId;
}
