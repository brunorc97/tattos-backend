package com.tattos.dto.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class ProjetoResponseDTO {
    @Schema(example = "1")private Long id;
    private String nome;
    private String descricao;
    private boolean aprovado;
    private java.time.LocalDateTime dataCriacao;
}
