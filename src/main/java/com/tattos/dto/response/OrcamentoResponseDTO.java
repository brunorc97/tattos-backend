package com.tattos.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class OrcamentoResponseDTO {
    @Schema(example = "1")private Long id;
    private String nome;
    private BigDecimal valor;
    private LocalDateTime dataCriacao;
    private Long projetoId;
}
