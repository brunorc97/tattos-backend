package com.tattos.dto.response;

import com.tattos.model.Insumo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsumoResponseDTO {

    private Long id;
    private String codigo;
    private String nome;
    private Integer quantidadeAtual;
    private Integer nivelCritico;
    private boolean alertaCritico;
    private String unidadeMedida;
    private String categoria;
    private String lote;
    private LocalDate dataValidade;
    private BigDecimal valorUnitario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InsumoResponseDTO fromEntity(Insumo i) {
        if (i == null) return null;
        return InsumoResponseDTO.builder()
                .id(i.getId())
                .codigo(i.getCodigo())
                .nome(i.getNome())
                .quantidadeAtual(i.getQuantidadeAtual())
                .nivelCritico(i.getNivelCritico())
                .alertaCritico(i.isEstoqueCritico())
                .unidadeMedida(i.getUnidadeMedida())
                .categoria(i.getCategoria())
                .lote(i.getLote())
                .dataValidade(i.getDataValidade())
                .valorUnitario(i.getValorUnitario())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }
}
