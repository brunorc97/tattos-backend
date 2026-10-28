package com.tattos.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO usado para solicitar a geração do código PIX do sinal com base em um percentual
 * informado pelo cliente ou gestor. O percentual deve estar entre 10 e 50.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PixSinalRequestDTO {

    @NotNull(message = "{validation.sinal.percentual.required}")
    @Min(value = 10, message = "{validation.sinal.percentual.min}")
    @Max(value = 50, message = "{validation.sinal.percentual.max}")
    private Integer percentual;
}
