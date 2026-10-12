package com.tattos.dto.request;

import lombok.Data;

@Data
public class ProjetoRequestDTO {
    private String nome;
    private String descricao;
    private Boolean aprovado; // optional when creating, approval handled via separate endpoint
}
