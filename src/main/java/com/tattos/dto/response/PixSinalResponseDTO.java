package com.tattos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta ao gerar código PIX do sinal de pagamento.
 * Contém o código "copia e cola" (string) e o valor calculado
 * a partir do percentual informado.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixSinalResponseDTO {
    /** Código Pix (copia e cola) */
    private String codigo;
    /** Valor do sinal em centavos (ex.: 1500 = R$ 15,00) */
    private Long valorCentavos;
    /** Percentual aplicado ao valor total da sessão */
    private Integer percentual;
}
