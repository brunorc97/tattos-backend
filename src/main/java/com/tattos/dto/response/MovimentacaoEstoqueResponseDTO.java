package com.tattos.dto.response;

import com.tattos.model.MovimentacaoEstoque;
import com.tattos.model.enums.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimentacaoEstoqueResponseDTO {

    private Long id;
    private Long insumoId;
    private TipoMovimentacao tipo;
    private Integer quantidade;
    private BigDecimal custoUnitario;
    private Long retiradoPorId;
    private String retiradoPorNome;
    private String motivo;
    private String createdBy;
    private LocalDateTime createdAt;

    public static MovimentacaoEstoqueResponseDTO fromEntity(MovimentacaoEstoque m) {
        if (m == null) return null;
        return MovimentacaoEstoqueResponseDTO.builder()
                .id(m.getId())
                .insumoId(m.getInsumo() != null ? m.getInsumo().getId() : null)
                .tipo(m.getTipo())
                .quantidade(m.getQuantidade())
                .custoUnitario(m.getCustoUnitario())
                .retiradoPorId(m.getRetiradoPor() != null ? m.getRetiradoPor().getId() : null)
                .retiradoPorNome(m.getRetiradoPor() != null ? m.getRetiradoPor().getNome() : null)
                .motivo(m.getMotivo())
                .createdBy(m.getCreatedBy())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
