package com.tattos.repository;

import com.tattos.model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByCode(String code);

    List<StockItem> findByCategoryIgnoreCase(String category);

    // Alertas de ruptura: itens onde a quantidade atual é menor ou igual ao mínimo de alerta
    @Query("SELECT s FROM StockItem s WHERE s.quantity <= s.minQuantity ORDER BY s.quantity ASC")
    List<StockItem> findRuptureAlerts();

    @Query("SELECT s FROM StockItem s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.brand) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<StockItem> searchStockItems(@Param("query") String query);
}
