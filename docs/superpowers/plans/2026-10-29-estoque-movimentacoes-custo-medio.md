# Estoque — Movimentações com Custo, Média e Responsável — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Registrar histórico de movimentações de estoque (entrada com custo → custo médio simples; saída com quem retirou), mantendo compatibilidade com os endpoints PATCH atuais (deprecated).

**Architecture:** Nova entidade `MovimentacaoEstoque` + migration V29. `InsumoService` passa a gravar movimentação em cada ajuste e recalcula `valorUnitario` como média simples dos custos de entrada. Novos DTOs estendem o ajuste com `custoUnitario` (entrada) e `retiradoPorId` (saída).

**Tech Stack:** Spring Boot 4, Spring Data JPA, Flyway, Bean Validation, JUnit 5 + MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-29-pacote-evolucoes-design.md` (frente 2)

## Global Constraints

- Migration: **V29** (`V29__movimentacao_estoque.sql`), padrão `V{n}__snake_case.sql`.
- Custo médio = **média simples** de todos os `custo_unitario` das movimentações de ENTRADA (decisão do Bruno — não ponderar).
- Saída: `retiradoPorId` obrigatório (tatuador ou gestor existente e ativo); GESTOR pode registrar em nome de tatuador — `createdBy` de auditoria guarda quem registrou.
- PATCHs antigos `/entrada` e `/saida` continuam funcionando (deprecated no OpenAPI).
- Testes: padrão repo (`@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional`, H2).
- Commits: conventional commits em português.

## Review Focus

1. Entrada sem custo informado → movimentação gravada sem custo e **não** entra no cálculo da média (não derruba a média para zero) — teste Task 3.
2. Saída com `retiradoPorId` de usuário inexistente/inativo ou CLIENTE → 400/422 (BusinessException) — teste Task 4.
3. Primeira entrada de um insumo sem nenhum custo anterior → média = custo da entrada — teste Task 3.
4. PATCH antigo (sem campos novos) continuar funcionando para não quebrar o front atual — teste de regressão Task 5.
5. Saldo insuficiente na saída → BusinessException existente preservada, e **nenhuma** movimentação gravada (ordem: validar saldo antes de persistir) — teste Task 4.

---

### Task 1: Migration V29 + entidade MovimentacaoEstoque

**Files:**
- Create: `src/main/resources/db/migration/V29__movimentacao_estoque.sql`
- Create: `src/main/java/com/tattos/model/MovimentacaoEstoque.java`
- Create: `src/main/java/com/tattos/repository/MovimentacaoEstoqueRepository.java`

**Interfaces:**
- Produces:
  - `MovimentacaoEstoque` campos: `id`, `insumo` (ManyToOne, obrigatório), `tipo` (enum `TipoMovimentacao { ENTRADA, SAIDA }`, STRING), `quantidade` (int), `custoUnitario` (BigDecimal, nullable — só entrada), `retiradoPor` (ManyToOne Usuario, nullable — só saída), `motivo` (String), `createdBy` (@CreatedBy), `createdAt` (@CreatedDate).
  - `MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>` com `List<MovimentacaoEstoque> findByInsumoIdOrderByCreatedAtDesc(Long insumoId)` e `@Query("SELECT AVG(m.custoUnitario) FROM MovimentacaoEstoque m WHERE m.insumo.id = :insumoId AND m.tipo = 'ENTRADA' AND m.custoUnitario IS NOT NULL") BigDecimal mediaCustosEntrada(Long insumoId)`.

- [ ] **Step 1: Criar migration**

```sql
CREATE TABLE movimentacao_estoque (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    quantidade INT NOT NULL,
    custo_unitario DECIMAL(10,2) NULL,
    retirado_por_id BIGINT NULL,
    motivo VARCHAR(255) NULL,
    created_by VARCHAR(150) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_insumo FOREIGN KEY (insumo_id) REFERENCES insumos(id),
    CONSTRAINT fk_mov_retirado_por FOREIGN KEY (retirado_por_id) REFERENCES usuarios(id)
);
```

(Confirmar nomes reais das tabelas `insumos`/`usuarios` nas entidades antes de escrever.)

- [ ] **Step 2: Criar entidade e repositório**

Seguir padrão de `TrabalhoRealizado` (Lombok `@Getter/@Setter/@Builder`, `@EntityListeners(AuditingEntityListener.class)`). Enum `TipoMovimentacao` em `com.tattos.model.enums`.

- [ ] **Step 3: Verificar que o contexto sobe e a migration aplica**

Run: `./gradlew test --tests "com.tattos.TattosApplicationTests"`
Expected: PASS

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/migration/V29__movimentacao_estoque.sql src/main/java/com/tattos/model/MovimentacaoEstoque.java src/main/java/com/tattos/model/enums/TipoMovimentacao.java src/main/java/com/tattos/repository/MovimentacaoEstoqueRepository.java
git commit -m "feat(estoque): criar entidade e migration de movimentacao de estoque"
```

---

### Task 2: DTOs de entrada/saída com novos campos

**Files:**
- Create: `src/main/java/com/tattos/dto/request/EntradaEstoqueDTO.java`
- Create: `src/main/java/com/tattos/dto/request/SaidaEstoqueDTO.java`
- Create: `src/main/java/com/tattos/dto/response/MovimentacaoEstoqueResponseDTO.java`

**Interfaces:**
- Produces:
  - `EntradaEstoqueDTO { @NotNull @Min(1) Integer quantidade; @DecimalMin("0.00") BigDecimal custoUnitario; String motivo; }`
  - `SaidaEstoqueDTO { @NotNull @Min(1) Integer quantidade; @NotNull Long retiradoPorId; String motivo; }`
  - `MovimentacaoEstoqueResponseDTO { id, insumoId, tipo, quantidade, custoUnitario, retiradoPorId, retiradoPorNome, motivo, createdBy, createdAt; static fromEntity(MovimentacaoEstoque) }`

- [ ] **Step 1: Criar os DTOs** (seguir padrão de `AjusteEstoqueDTO` com mensagens `{validation.*}` existentes; adicionar chave nova em `messages*.properties` se necessário para `retiradoPorId` obrigatório).

- [ ] **Step 2: Rodar teste de validação existente para garantir que nada quebrou**

Run: `./gradlew test --tests "com.tattos.controller.ValidationIntegrationTest"`
Expected: PASS

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/tattos/dto/request/EntradaEstoqueDTO.java src/main/java/com/tattos/dto/request/SaidaEstoqueDTO.java src/main/java/com/tattos/dto/response/MovimentacaoEstoqueResponseDTO.java src/main/resources/messages*.properties
git commit -m "feat(estoque): DTOs de entrada/saida com custo e responsavel"
```

---

### Task 3: Entrada com custo + média simples

**Files:**
- Modify: `src/main/java/com/tattos/service/InsumoService.java`
- Test: `src/test/java/com/tattos/service/InsumoServiceTest.java` (criar se não existir)

**Interfaces:**
- Consumes: `MovimentacaoEstoqueRepository` (Task 1), `EntradaEstoqueDTO` (Task 2).
- Produces: `InsumoService.registrarEntrada(Long insumoId, EntradaEstoqueDTO dto): InsumoResponseDTO` — adiciona saldo, grava movimentação, e se `custoUnitario != null` recalcula `insumo.valorUnitario = mediaCustosEntrada(insumoId)` (média simples, escala 2, RoundingMode.HALF_UP).

- [ ] **Step 1: Write the failing tests**

```java
@Test void entradaComCustoAtualizaMediaSimples() {
    // insumo com entrada de 10un a 2.00 depois 10un a 4.00 -> valorUnitario = 3.00
}
@Test void entradaSemCustoNaoDerrubaMedia() {
    // media existente 3.00; entrada sem custo -> valorUnitario continua 3.00
}
@Test void primeiraEntradaDefineMedia() { /* sem histórico -> media = custo da entrada */ }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "com.tattos.service.InsumoServiceTest"`
Expected: FAIL — método não existe.

- [ ] **Step 3: Implementar `registrarEntrada`**

Ordem: `insumo.adicionarEstoque(qtd)` → salvar movimentação ENTRADA → se custo informado, recalcular média via query do repositório e atualizar `valorUnitario` → salvar insumo → retornar DTO.

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.tattos.service.InsumoServiceTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/tattos/service/InsumoService.java src/test/java/com/tattos/service/InsumoServiceTest.java
git commit -m "feat(estoque): entrada com custo e custo medio simples"
```

---

### Task 4: Saída com responsável

**Files:**
- Modify: `src/main/java/com/tattos/service/InsumoService.java`
- Modify: `src/test/java/com/tattos/service/InsumoServiceTest.java`

**Interfaces:**
- Produces: `InsumoService.registrarSaida(Long insumoId, SaidaEstoqueDTO dto): InsumoResponseDTO` — valida saldo primeiro (BusinessException existente), valida `retiradoPorId` (existe, ativo, role TATUADOR ou GESTOR — senão BusinessException), grava movimentação SAIDA com `retiradoPor`.

- [ ] **Step 1: Write the failing tests**

```java
@Test void saidaRegistraQuemRetirou() { /* movimentacao SAIDA com retiradoPor = tatuador */ }
@Test void saidaComRetiradoPorClienteRetornaErro() { /* BusinessException */ }
@Test void saidaComSaldoInsuficienteNaoGravaMovimentacao() { /* repo vazio para o insumo */ }
```

- [ ] **Step 2: Run to fail** — `./gradlew test --tests "com.tattos.service.InsumoServiceTest"` → FAIL.

- [ ] **Step 3: Implementar** `registrarSaida` (validar saldo via `removerEstoque`; validar usuário via `UsuarioRepository`; gravar movimentação).

- [ ] **Step 4: Run to pass** — Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(estoque): saida com responsavel pela retirada"
```

---

### Task 5: Endpoints POST + histórico + deprecar PATCHs

**Files:**
- Modify: `src/main/java/com/tattos/controller/InsumoController.java`
- Test: `src/test/java/com/tattos/controller/InsumoMovimentacaoControllerTest.java`

**Interfaces:**
- Consumes: `registrarEntrada`/`registrarSaida` (Tasks 3-4), `MovimentacaoEstoqueRepository` (Task 1).
- Produces:
  - `POST /api/v1/estoque/{id}/entrada` (body `EntradaEstoqueDTO`) — GESTOR, TATUADOR
  - `POST /api/v1/estoque/{id}/saida` (body `SaidaEstoqueDTO`) — GESTOR, TATUADOR
  - `GET /api/v1/estoque/{id}/movimentacoes` → lista `MovimentacaoEstoqueResponseDTO` (mais recentes primeiro) — GESTOR, TATUADOR
  - PATCHs antigos anotados `@Deprecated` + `@Operation(deprecated = true)`; internamente delegam ao novo fluxo (entrada sem custo; saída com `retiradoPor` = usuário logado).

- [ ] **Step 1: Write the failing tests** (padrão MockMvc):
- `postEntradaComCustoRetorna200EAtualizaValorMedio()`
- `postSaidaSemRetiradoPorIdRetorna400()`
- `getMovimentacoesListaMaisRecentesPrimeiro()`
- `patchAntigoContinuaFuncionando()` (regressão de compatibilidade)
- `clienteNaoAcessaMovimentacoesRetorna403()`

- [ ] **Step 2: Run to fail** — `./gradlew test --tests "com.tattos.controller.InsumoMovimentacaoControllerTest"` → FAIL (404).

- [ ] **Step 3: Implementar os 3 endpoints + deprecação dos PATCHs.**

- [ ] **Step 4: Run to pass** + regressão: `./gradlew test --tests "com.tattos.controller.Insumo*"` → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(estoque): endpoints de movimentacao e historico; PATCHs antigos deprecated"
```

---

### Task 6: Verificação final

- [ ] **Step 1:** `./gradlew test` — suíte completa PASS.
- [ ] **Step 2:** Commit de documentação se necessário (OpenAPI cobre via anotações; README já lista Estoque como módulo).
