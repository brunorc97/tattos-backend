package com.tattos.repository;

import com.tattos.model.FinancialTransaction;
import com.tattos.model.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    List<FinancialTransaction> findByTypeOrderByTransactionDateDesc(TransactionType type);

    List<FinancialTransaction> findAllByOrderByTransactionDateDescCreatedAtDesc();

    List<FinancialTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FinancialTransaction t WHERE t.type = :type")
    BigDecimal sumAmountByType(@Param("type") TransactionType type);

    @Query("SELECT t FROM FinancialTransaction t WHERE t.artist.id = :artistId AND t.type = 'INCOME' ORDER BY t.transactionDate DESC")
    List<FinancialTransaction> findIncomesByArtist(@Param("artistId") Long artistId);
}
