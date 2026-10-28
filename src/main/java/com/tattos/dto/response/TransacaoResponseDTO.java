package com.tattos.dto.response;

import com.tattos.model.TransacaoFinanceira;
import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoResponseDTO {

    @Schema(example = "1")private Long id;
    private String descricao;
    private BigDecimal valor;
    private TipoTransacao tipo;
    private LocalDate data;
    private StatusTransacao status;
    private Long tatuadorId;
    private String tatuadorNome;
    private String categoria;
    private String formaPagamento;
    private BigDecimal comissaoPercentual;
    private BigDecimal valorRepasse;
    private Long agendamentoId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransacaoResponseDTO fromEntity(TransacaoFinanceira t) {
        if (t == null) return null;
        return TransacaoResponseDTO.builder()
                .id(t.getId())
                .descricao(t.getDescricao())
                .valor(t.getValor())
                .tipo(t.getTipo())
                .data(t.getData())
                .status(t.getStatus())
                .tatuadorId(t.getTatuador() != null ? t.getTatuador().getId() : null)
                .tatuadorNome(t.getTatuador() != null ? t.getTatuador().getNome() : null)
                .categoria(t.getCategoria())
                .formaPagamento(t.getFormaPagamento())
                .comissaoPercentual(t.getComissaoPercentual())
                .valorRepasse(t.getValorRepasse())
                .agendamentoId(t.getAgendamento() != null ? t.getAgendamento().getId() : null)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
