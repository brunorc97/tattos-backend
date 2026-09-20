package com.tattos.dto.response;

import com.tattos.model.StockItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record StockItemResponseDTO(
    Long id,
    String code,
    String name,
    String category,
    String brand,
    Integer quantity,
    Integer minQuantity,
    String unit,
    String batch,
    LocalDate expiryDate,
    BigDecimal costPerUnit,
    boolean alertCritical,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static StockItemResponseDTO fromEntity(StockItem item) {
        if (item == null) return null;
        boolean alert = item.getQuantity() != null && item.getMinQuantity() != null
                && item.getQuantity() <= item.getMinQuantity();

        return new StockItemResponseDTO(
            item.getId(),
            item.getCode(),
            item.getName(),
            item.getCategory(),
            item.getBrand(),
            item.getQuantity(),
            item.getMinQuantity(),
            item.getUnit(),
            item.getBatch(),
            item.getExpiryDate(),
            item.getCostPerUnit(),
            alert,
            item.getCreatedAt(),
            item.getUpdatedAt()
        );
    }
}
