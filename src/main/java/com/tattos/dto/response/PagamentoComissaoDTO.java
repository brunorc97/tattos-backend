package com.tattos.dto.response;

import com.tattos.model.PagamentoComissao;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagamentoComissaoDTO {

    private Long id;
    private Long tatuadorId;
    private String tatuadorNome;
    private BigDecimal totalBruto;
    private BigDecimal totalAdiantamentosAbatidos;
    private BigDecimal totalLiquido;
    private String formaPagamento;
    private String status;
    private Long comprovanteId;
    private Long reciboArquivoId;
    private List<Item> itens;
    private LocalDateTime createdAt;
    private LocalDateTime pagoEm;
    private java.time.LocalDate periodoInicio;
    private java.time.LocalDate periodoFim;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private Long transacaoId;
        private String descricao;
        private BigDecimal valorRepasse;
    }

    public static PagamentoComissaoDTO fromEntity(PagamentoComissao p) {
        return PagamentoComissaoDTO.builder()
                .id(p.getId())
                .tatuadorId(p.getTatuador().getId())
                .tatuadorNome(p.getTatuador().getNome())
                .totalBruto(p.getTotalBruto())
                .totalAdiantamentosAbatidos(p.getTotalAdiantamentosAbatidos())
                .totalLiquido(p.getTotalLiquido())
                .formaPagamento(p.getFormaPagamento())
                .status(p.getStatus().name())
                .comprovanteId(p.getComprovante() != null ? p.getComprovante().getId() : null)
                .reciboArquivoId(p.getReciboArquivo() != null ? p.getReciboArquivo().getId() : null)
                .itens(p.getItens() == null ? List.of() : p.getItens().stream()
                        .map(i -> Item.builder()
                                .transacaoId(i.getTransacao().getId())
                                .descricao(i.getTransacao().getDescricao())
                                .valorRepasse(i.getValorRepasse())
                                .build())
                        .toList())
                .createdAt(p.getCreatedAt())
                .pagoEm(p.getPagoEm())
                .periodoInicio(p.getPeriodoInicio())
                .periodoFim(p.getPeriodoFim())
                .build();
    }
}
