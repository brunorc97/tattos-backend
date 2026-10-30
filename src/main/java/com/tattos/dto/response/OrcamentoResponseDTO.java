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
    private com.tattos.model.enums.TipoCobranca tipoCobranca;
    private BigDecimal valorTotal;
    private BigDecimal valorSessao;
    private Integer sessoesMin;
    private Integer sessoesMax;
    private String observacoes;
    private com.tattos.model.enums.StatusOrcamento status;
}
