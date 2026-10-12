package com.tattos.dto.response;

import lombok.Data;

@Data
public class ProjetoResponseDTO {
    private Long id;
    private String nome;
    private String descricao;
    private boolean aprovado;
    private java.time.LocalDateTime dataCriacao;
}
