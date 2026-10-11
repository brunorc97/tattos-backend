package com.tattos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FechamentoComissaoDTO {

    private Long tatuadorId;
    private String tatuadorNome;
    private String especialidade;
    private BigDecimal comissaoPercentualPadrao;
    private LocalDate periodoInicio;
    private LocalDate periodoFim;
    private int totalAtendimentos;
    private BigDecimal totalBruto;
    private BigDecimal totalComissaoRepasse;
    private BigDecimal totalRetidoEstudio;
    private List<ItemComissaoDTO> itens;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemComissaoDTO {
        private Long transacaoId;
        private Long agendamentoId;
        private LocalDate data;
        private String descricao;
        private BigDecimal valorBruto;
        private BigDecimal comissaoPercentual;
        private BigDecimal valorRepasse;
        private String formaPagamento;
    }
}
