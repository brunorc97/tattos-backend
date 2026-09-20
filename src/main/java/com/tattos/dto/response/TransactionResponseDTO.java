package com.tattos.dto.response;

import com.tattos.model.FinancialTransaction;
import com.tattos.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponseDTO(
    Long id,
    String code,
    TransactionType type,
    String title,
    BigDecimal amount,
    String category,
    String paymentMethod,
    String method,
    LocalDate transactionDate,
    LocalDate date,
    String notes,
    String clientName,
    String client,
    Long artistId,
    String artist,
    String supplier,
    String responsible,
    LocalDateTime createdAt
) {
    public static TransactionResponseDTO fromEntity(FinancialTransaction tx) {
        if (tx == null) return null;
        String artistName = tx.getArtist() != null ? tx.getArtist().getName() : null;
        Long artistIdVal = tx.getArtist() != null ? tx.getArtist().getId() : null;

        return new TransactionResponseDTO(
            tx.getId(),
            tx.getCode(),
            tx.getType(),
            tx.getTitle(),
            tx.getAmount(),
            tx.getCategory(),
            tx.getPaymentMethod(),
            tx.getPaymentMethod(),
            tx.getTransactionDate(),
            tx.getTransactionDate(),
            tx.getNotes(),
            tx.getClientName(),
            tx.getClientName(),
            artistIdVal,
            artistName,
            tx.getSupplier(),
            tx.getResponsible(),
            tx.getCreatedAt()
        );
    }
}
