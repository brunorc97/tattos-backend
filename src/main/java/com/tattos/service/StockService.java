package com.tattos.service;

import com.tattos.dto.request.StockAdjustmentRequestDTO;
import com.tattos.dto.request.StockItemRequestDTO;
import com.tattos.dto.response.StockItemResponseDTO;
import com.tattos.exception.BusinessException;
import com.tattos.exception.ResourceNotFoundException;
import com.tattos.model.StockItem;
import com.tattos.repository.StockItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository stockItemRepository;

    @Transactional(readOnly = true)
    public List<StockItemResponseDTO> findAll(String category, String query) {
        List<StockItem> items;

        if (query != null && !query.isBlank()) {
            items = stockItemRepository.searchStockItems(query);
        } else if (category != null && !category.isBlank()) {
            items = stockItemRepository.findByCategoryIgnoreCase(category);
        } else {
            items = stockItemRepository.findAll();
        }

        return items.stream().map(StockItemResponseDTO::fromEntity).toList();
    }

    /**
     * Endpoint crítico: Alertas de ruptura de insumos (quantity <= minQuantity)
     */
    @Transactional(readOnly = true)
    public List<StockItemResponseDTO> findRuptureAlerts() {
        return stockItemRepository.findRuptureAlerts().stream()
                .map(StockItemResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public StockItemResponseDTO findResponseById(Long id) {
        return StockItemResponseDTO.fromEntity(findEntityById(id));
    }

    @Transactional(readOnly = true)
    public StockItem findEntityById(Long id) {
        return stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado com o ID: " + id));
    }

    @Transactional
    public StockItemResponseDTO create(StockItemRequestDTO dto) {
        String code = dto.code();
        if (code == null || code.isBlank()) {
            long count = stockItemRepository.count() + 1;
            code = String.format("EST-%02d", count);
        }

        StockItem item = StockItem.builder()
                .code(code)
                .name(dto.name())
                .category(dto.category())
                .brand(dto.brand())
                .quantity(dto.quantity())
                .minQuantity(dto.minQuantity())
                .unit(dto.unit())
                .batch(dto.batch())
                .expiryDate(dto.expiryDate())
                .costPerUnit(dto.costPerUnit())
                .build();

        StockItem saved = stockItemRepository.save(item);
        return StockItemResponseDTO.fromEntity(saved);
    }

    @Transactional
    public StockItemResponseDTO update(Long id, StockItemRequestDTO dto) {
        StockItem existing = findEntityById(id);

        if (dto.code() != null && !dto.code().isBlank()) {
            existing.setCode(dto.code());
        }
        existing.setName(dto.name());
        existing.setCategory(dto.category());
        existing.setBrand(dto.brand());
        existing.setQuantity(dto.quantity());
        existing.setMinQuantity(dto.minQuantity());
        existing.setUnit(dto.unit());
        existing.setBatch(dto.batch());
        existing.setExpiryDate(dto.expiryDate());
        existing.setCostPerUnit(dto.costPerUnit());

        StockItem saved = stockItemRepository.save(existing);
        return StockItemResponseDTO.fromEntity(saved);
    }

    @Transactional
    public StockItemResponseDTO adjust(Long id, StockAdjustmentRequestDTO dto) {
        StockItem existing = findEntityById(id);
        int newQuantity = existing.getQuantity() + dto.amount();

        if (newQuantity < 0) {
            throw new BusinessException(String.format(
                    "Operação inválida! A quantidade resultante em estoque não pode ser negativa (Atual: %d, Ajuste: %d).",
                    existing.getQuantity(),
                    dto.amount()
            ));
        }

        existing.setQuantity(newQuantity);
        StockItem saved = stockItemRepository.save(existing);
        return StockItemResponseDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        StockItem existing = findEntityById(id);
        stockItemRepository.delete(existing);
    }
}
