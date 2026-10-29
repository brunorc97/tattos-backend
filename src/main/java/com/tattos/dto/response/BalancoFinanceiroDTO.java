package com.tattos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Balanço Gerencial "Smile Now, Cry Later":
 * Confronta o faturamento bruto (Smile Now) com custos fixos, insumos e repasses de comissão (Cry Later).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BalancoFinanceiroDTO {

    private int mes;
    private int ano;

    // Smile Now
    private BigDecimal faturamentoBruto;
    private BigDecimal faturamentoLiquido;
    private BigDecimal totalTaxas;

    // Cry Later
    private BigDecimal totalDespesas;
    private BigDecimal totalRepassesTatuadores;
    private BigDecimal totalSaidas;

    // Resultado Líquido Operacional
    private BigDecimal lucroLiquido;
    private BigDecimal margemLucroPercentual;

    private int totalTransacoes;
}
