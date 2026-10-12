package com.tattos.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrcamentoRequestDTO {
    private String nome;
    private BigDecimal valor;
    private Long projetoId;
}
