package com.tattos.dto.request;

import jakarta.validation.constraints.NotNull;

public record StockAdjustmentRequestDTO(
    @NotNull(message = "A quantidade de ajuste é obrigatória (positiva para entrada, negativa para saída)")
    Integer amount,

    String reason
) {}
