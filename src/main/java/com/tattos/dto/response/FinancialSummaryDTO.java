package com.tattos.dto.response;

import java.math.BigDecimal;

public record FinancialSummaryDTO(
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal netBalance,
    long incomeCount,
    long expenseCount,
    BigDecimal avgIncomeTicket,
    BigDecimal avgExpenseTicket,
    double operatingMarginPercentage
) {}
