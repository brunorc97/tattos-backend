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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoRequestDTO {

    @NotBlank(message = "{validation.descricao.required}")
    private String descricao;

    @NotNull(message = "{validation.valor.required}")
    @DecimalMin(value = "0.01", message = "{validation.positive}")
    private BigDecimal valor;

    @NotNull(message = "{validation.transacao.tipo.required}")
    private TipoTransacao tipo;

    @NotNull(message = "{validation.data.required}")
    private LocalDate data;

    @Builder.Default
    private StatusTransacao status = StatusTransacao.CONCLUIDO;

    private Long tatuadorId;

    private String categoria;

    private String formaPagamento;

    private BigDecimal comissaoPercentual;

    private Long agendamentoId;
}
