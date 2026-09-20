package com.tattos.controller;

import com.tattos.dto.request.StockAdjustmentRequestDTO;
import com.tattos.dto.request.StockItemRequestDTO;
import com.tattos.dto.response.StockItemResponseDTO;
import com.tattos.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockItemResponseDTO>> listAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query) {
        return ResponseEntity.ok(stockService.findAll(category, query));
    }

    /**
     * Endpoint crítico: Alertas de ruptura de estoque
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<StockItemResponseDTO>> getRuptureAlerts() {
        return ResponseEntity.ok(stockService.findRuptureAlerts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockItemResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.findResponseById(id));
    }

    @PostMapping
    public ResponseEntity<StockItemResponseDTO> create(@Valid @RequestBody StockItemRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockItemResponseDTO> update(@PathVariable Long id, @Valid @RequestBody StockItemRequestDTO dto) {
        return ResponseEntity.ok(stockService.update(id, dto));
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<StockItemResponseDTO> adjust(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequestDTO dto) {
        return ResponseEntity.ok(stockService.adjust(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
