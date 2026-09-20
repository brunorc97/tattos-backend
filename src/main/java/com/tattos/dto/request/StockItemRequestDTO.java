package com.tattos.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record StockItemRequestDTO(
    String code,

    @NotBlank(message = "O nome do item é obrigatório")
    String name,

    @NotBlank(message = "A categoria é obrigatória")
    String category,

    String brand,

    @NotNull(message = "A quantidade atual é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    Integer quantity,

    @NotNull(message = "A quantidade mínima de alerta é obrigatória")
    @Min(value = 0, message = "A quantidade mínima não pode ser negativa")
    Integer minQuantity,

    @NotBlank(message = "A unidade de medida é obrigatória")
    String unit,

    String batch,
    LocalDate expiryDate,

    @DecimalMin(value = "0.00", message = "O custo unitário não pode ser negativo")
    BigDecimal costPerUnit
) {}
