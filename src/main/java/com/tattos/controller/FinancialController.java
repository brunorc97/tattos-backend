package com.tattos.controller;

import com.tattos.dto.request.TransactionRequestDTO;
import com.tattos.dto.response.FinancialSummaryDTO;
import com.tattos.dto.response.TransactionResponseDTO;
import com.tattos.model.enums.TransactionType;
import com.tattos.service.FinancialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financial")
@RequiredArgsConstructor
public class FinancialController {

    private final FinancialService financialService;

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> listTransactions(
            @RequestParam(required = false) TransactionType type) {
        return ResponseEntity.ok(financialService.findTransactions(type));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(financialService.findById(id));
    }

    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDTO> getSummary() {
        return ResponseEntity.ok(financialService.getSummary());
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponseDTO> create(@Valid @RequestBody TransactionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financialService.create(dto));
    }
}
