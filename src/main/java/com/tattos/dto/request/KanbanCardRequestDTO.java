package com.tattos.dto.request;

import lombok.Data;

@Data
public class KanbanCardRequestDTO {
    private String titulo;
    private String descricao;
    private String status;
    private Integer ordem;
    private Long orcamentoId;
    private Long projetoId;
}
