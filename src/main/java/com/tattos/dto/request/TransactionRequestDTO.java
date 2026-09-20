package com.tattos.dto.request;

import com.tattos.model.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequestDTO(
    @NotNull(message = "O tipo de transação (INCOME/EXPENSE) é obrigatório")
    TransactionType type,

    @NotBlank(message = "O título/descrição é obrigatório")
    String title,

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    BigDecimal amount,

    @NotBlank(message = "A categoria é obrigatória")
    String category,

    @NotBlank(message = "A forma de pagamento é obrigatória")
    String paymentMethod,

    @NotNull(message = "A data da transação é obrigatória")
    LocalDate transactionDate,

    String clientName,    // preenchido se type == INCOME
    Long artistId,        // opcional/recomendado para cálculo de repasse
    String supplier,      // preenchido se type == EXPENSE
    String responsible,   // preenchido se type == EXPENSE
    String notes
) {}
