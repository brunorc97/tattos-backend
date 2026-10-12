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

    @NotNull(message = "O percentual do sinal é obrigatório")
    @Min(value = 10, message = "O percentual mínimo permitido é 10")
    @Max(value = 50, message = "O percentual máximo permitido é 50")
    private Integer percentual;
}
