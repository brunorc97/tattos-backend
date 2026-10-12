package com.tattos.dto.response;

import lombok.Data;

@Data
public class KanbanCardResponseDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private String status;
    private Integer ordem;
    private Long orcamentoId;
    private Long projetoId;
}
