package com.tattos.dto.request;

import com.tattos.model.enums.StatusTransacao;
import com.tattos.model.enums.TipoTransacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoRequestDTO {

    @NotBlank(message = "{validation.descricao.required}")
        @Schema(example = "Sessão 2 - fechamento de braço")private String descricao;

    @NotNull(message = "{validation.valor.required}")
    @DecimalMin(value = "0.01", message = "{validation.positive}")
        @Schema(example = "1500.00")private BigDecimal valor;

    @NotNull(message = "{validation.transacao.tipo.required}")
        @Schema(example = "RECEITA")private TipoTransacao tipo;

    @NotNull(message = "{validation.data.required}")
        @Schema(example = "2026-10-28")private LocalDate data;

    @Builder.Default
        @Schema(example = "CONCLUIDO")private StatusTransacao status = StatusTransacao.CONCLUIDO;

    
    @Schema(example = "2")private Long tatuadorId;

    
    @Schema(example = "Procedimento Tattoo")private String categoria;

    
    @Schema(example = "PIX")private String formaPagamento;

    
    @Schema(example = "60.00")private BigDecimal comissaoPercentual;

    
    @Schema(example = "10")private Long agendamentoId;
}
