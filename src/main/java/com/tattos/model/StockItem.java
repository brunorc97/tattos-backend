package com.tattos.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code; // ex: EST-01

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String category; // Agulhas & Cartuchos, Tintas & Pigmentos, Biossegurança & EPI

    @Column(length = 100)
    private String brand;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "min_quantity", nullable = false)
    private Integer minQuantity;

    @Column(nullable = false, length = 30)
    private String unit; // unid., frascos, caixas, rolos

    @Column(length = 50)
    private String batch;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;

    @Builder.Default
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
