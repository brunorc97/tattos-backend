package com.tattos.service;

import com.tattos.dto.request.TransactionRequestDTO;
import com.tattos.dto.response.FinancialSummaryDTO;
import com.tattos.dto.response.TransactionResponseDTO;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.Artist;
import com.tattos.model.FinancialTransaction;
import com.tattos.model.enums.TransactionType;
import com.tattos.repository.ArtistRepository;
import com.tattos.repository.FinancialTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialService {

    private final FinancialTransactionRepository transactionRepository;
    private final ArtistRepository artistRepository;

    @Transactional(readOnly = true)
    public List<TransactionResponseDTO> findTransactions(TransactionType type) {
        List<FinancialTransaction> list = (type != null)
                ? transactionRepository.findByTypeOrderByTransactionDateDesc(type)
                : transactionRepository.findAllByOrderByTransactionDateDescCreatedAtDesc();

        return list.stream().map(TransactionResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO findById(Long id) {
        FinancialTransaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação financeira não encontrada com o ID: " + id));
        return TransactionResponseDTO.fromEntity(tx);
    }

    @Transactional
    public TransactionResponseDTO create(TransactionRequestDTO dto) {
        Artist artist = null;
        if (dto.artistId() != null) {
            artist = artistRepository.findById(dto.artistId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tatuador não encontrado com o ID: " + dto.artistId()));
        }

        String prefix = dto.type() == TransactionType.INCOME ? "REC" : "DES";
        String code = prefix + "-" + (System.currentTimeMillis() % 100000);

        FinancialTransaction tx = FinancialTransaction.builder()
                .code(code)
                .type(dto.type())
                .title(dto.title())
                .amount(dto.amount())
                .category(dto.category())
                .paymentMethod(dto.paymentMethod())
                .transactionDate(dto.transactionDate())
                .notes(dto.notes())
                .clientName(dto.clientName())
                .artist(artist)
                .supplier(dto.supplier())
                .responsible(dto.responsible())
                .build();

        FinancialTransaction saved = transactionRepository.save(tx);
        return TransactionResponseDTO.fromEntity(saved);
    }

    @Transactional
    public FinancialTransaction recordIncome(String title,
                                            BigDecimal amount,
                                            String category,
                                            String paymentMethod,
                                            LocalDate date,
                                            String clientName,
                                            Artist artist,
                                            String notes) {
        String code = "REC-" + (System.currentTimeMillis() % 100000);

        FinancialTransaction tx = FinancialTransaction.builder()
                .code(code)
                .type(TransactionType.INCOME)
                .title(title)
                .amount(amount)
                .category(category)
                .paymentMethod(paymentMethod)
                .transactionDate(date)
                .clientName(clientName)
                .artist(artist)
                .notes(notes)
                .build();

        return transactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public FinancialSummaryDTO getSummary() {
        BigDecimal totalIncome = transactionRepository.sumAmountByType(TransactionType.INCOME);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;

        BigDecimal totalExpense = transactionRepository.sumAmountByType(TransactionType.EXPENSE);
        if (totalExpense == null) totalExpense = BigDecimal.ZERO;

        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        long incomeCount = transactionRepository.findByTypeOrderByTransactionDateDesc(TransactionType.INCOME).size();
        long expenseCount = transactionRepository.findByTypeOrderByTransactionDateDesc(TransactionType.EXPENSE).size();

        BigDecimal avgIncome = incomeCount > 0
                ? totalIncome.divide(BigDecimal.valueOf(incomeCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal avgExpense = expenseCount > 0
                ? totalExpense.divide(BigDecimal.valueOf(expenseCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        double margin = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netBalance.divide(totalIncome, 4, RoundingMode.HALF_UP).doubleValue() * 100
                : 0.0;

        return new FinancialSummaryDTO(
                totalIncome,
                totalExpense,
                netBalance,
                incomeCount,
                expenseCount,
                avgIncome,
                avgExpense,
                margin
        );
    }
}
