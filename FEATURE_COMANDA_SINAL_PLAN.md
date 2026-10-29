# Feature Implementation Plan: Automatic Comanda Generation from Sinal (Advance Payment) and PIX

## Overview
- Add JPA entities **Comanda** and **Sinal**.
- Create repositories `ComandaRepository` and `SinalRepository`.
- Implement `SinalService` that saves a `Sinal` and automatically creates a linked `Comanda` when the sinal is marked as paid (or when a PIX code is generated).
- Extend `TransacaoService` to handle a new transaction type `TipoTransacao.ADIANTAMENTO` (or reuse existing) for sinal payments.
- Add REST controllers:
  - `SinalController` (`/api/financeiro/sinais`) for creating, retrieving, and generating PIX codes.
  - `ComandaController` (`/api/financeiro/comandas`) for CRUD operations.
- Write unit/integration tests for the new service and controller layers.
- Update README with a note about this new feature.

## Files to Add/Modify
| Path | Action |
|------|--------|
| `src/main/java/com/tattos/model/Comanda.java` | New entity file |
| `src/main/java/com/tattos/model/Sinal.java` | New entity file |
| `src/main/java/com/tattos/repository/ComandaRepository.java` | New repository |
| `src/main/java/com/tattos/repository/SinalRepository.java` | New repository |
| `src/main/java/com/tattos/service/SinalService.java` | New service |
| `src/main/java/com/tattos/controller/SinalController.java` | New controller |
| `src/main/java/com/tattos/controller/ComandaController.java` | New controller (or extend existing) |
| `src/main/resources/db/migration/V9__adicionar_comanda_sinal.sql` | Flyway migration for new tables |
| `src/test/java/com/tattos/service/SinalServiceTest.java` | Unit tests |
| `src/test/java/com/tattos/controller/SinalControllerTest.java` | Integration tests |
| `README.md` | Add a **Features** bullet about automatic comanda generation from sinal/PIX |

## Entity Sketches
```java
@Entity
@Table(name = "comandas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Comanda {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(nullable = false)
    private String status; // e.g., "ABERTA", "FECHADA"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```
```java
@Entity
@Table(name = "sinais")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sinal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "codigo_pix_sinal", length = 255)
    private String codigoPix;

    @Column(name = "pago", nullable = false)
    private boolean pago = false;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
```

## Service Logic (Simplified)
1. `SinalService.save(SinalRequestDTO dto)` creates a `Sinal` entity.
2. If `pago` is true or when a PIX code is generated, the service checks if a `Comanda` exists for the same `Agendamento`.
3. If not, it creates a new `Comanda` linked to that `Agendamento` with status `ABERTA`.
4. The `TransacaoService` can be called to create a financial transaction of type `ADIANTAMENTO` referencing the same `Agendamento`.

## Controller Endpoints
- **POST** `/api/financeiro/sinais` → create sinal (accepts `PixSinalRequestDTO` + `valor`).
- **GET** `/api/financeiro/sinais/{id}` → retrieve sinal details.
- **POST** `/api/financeiro/sinais/{id}/gerar-pix` → generate PIX code, mark `pago=true`, trigger comanda creation.
- **GET** `/api/financeiro/comandas/{id}` → get comanda.
- **GET** `/api/financeiro/comandas` → list comandas (optional filters).

## README Update
Add a bullet under **Features**:
```
- Automatic creation of a **Comanda** when a **Sinal** (advance payment) is confirmed via PIX. The system now tracks advance payments and links them to a comanda for later settlement.
```

## Next Steps
1. Add migration file `V9__adicionar_comanda_sinal.sql` with tables `comandas` and `sinais`.
2. Implement the Java classes and repositories.
3. Write service and controller code.
4. Add tests.
5. Update README.

---
*All file paths are relative to the project root.*
