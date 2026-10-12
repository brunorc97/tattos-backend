package com.tattos.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrcamentoResponseDTO {
    private Long id;
    private String nome;
    private BigDecimal valor;
    private LocalDateTime dataCriacao;
    private Long projetoId;
}
