package com.tattos.dto/response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Resposta da geração do código PIX do sinal.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixSinalResponseDTO {
    private String codigoPix;
    private BigDecimal valorSinal;
}
