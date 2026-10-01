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

    @NotBlank(message = "A descrição é obrigatória")
    private String descricao;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "O tipo da transação é obrigatório")
    private TipoTransacao tipo;

    @NotNull(message = "A data é obrigatória")
    private LocalDate data;

    @Builder.Default
    private StatusTransacao status = StatusTransacao.CONCLUIDO;

    private Long tatuadorId;

    private String categoria;

    private String formaPagamento;

    private BigDecimal comissaoPercentual;

    private Long agendamentoId;
}
