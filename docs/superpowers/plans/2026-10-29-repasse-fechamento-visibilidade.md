# Repasse/Fechamento — Visibilidade de Pago + Período no Recibo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expor o status de repasse (PENDENTE/AGENDADO/PAGO) nas transações e no fechamento, e incluir o período do fechamento no recibo PDF.

**Architecture:** Mudanças incrementais sobre o módulo financeiro existente: DTOs ganham campos, `PagamentoComissao` ganha `periodoInicio/periodoFim` (migration V28), novo endpoint JSON de fechamento no `ComissaoController`, PDF ganha linha de período.

**Tech Stack:** Spring Boot 4, Spring Data JPA, Flyway, OpenHTMLtoPDF, JUnit 5 + MockMvc (`@SpringBootTest`, profile `test`, H2).

**Spec:** `docs/superpowers/specs/2026-10-29-pacote-evolucoes-design.md` (frente 5)

## Global Constraints

- Migrations seguem `V{n}__snake_case.sql` em `src/main/resources/db/migration/`; última existente é V27 — começar em **V28**.
- Testes de integração seguem o padrão do repo: `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional` (spec mencionava Testcontainers; o repo usa H2 — manter H2).
- Não alterar formato de payloads já consumidos pelo front sem necessidade (aditivos apenas).
- Commits em português, estilo conventional commits do histórico (`feat(financeiro): ...`).

## Review Focus

1. `TransacaoResponseDTO.fromEntity` usado em vários endpoints — o campo novo `statusRepasse` não pode quebrar serialização de quem não espera (aditivo, ok) nem vir null para transações antigas → default PENDENTE já existe na entidade; teste no Task 1 garante não-null.
2. Fechamento sem transações no período → retornar 200 com totais zerados e lista vazia (teste Task 3).
3. Período ausente em pagamentos antigos (linhas anteriores à migration) → PDF mostra "—" sem quebrar (teste Task 4).
4. `inicio` > `fim` no endpoint de fechamento → 400 via validação (teste Task 3).
5. TATUADOR pedindo fechamento de outro tatuador → 403 (teste Task 3).

---

### Task 1: Expor statusRepasse nos DTOs de transação e fechamento

**Files:**
- Modify: `src/main/java/com/tattos/dto/response/TransacaoResponseDTO.java`
- Modify: `src/main/java/com/tattos/dto/response/FechamentoComissaoDTO.java` (inner `ItemComissaoDTO`)
- Test: `src/test/java/com/tattos/dto/TransacaoResponseDTOTest.java`

**Interfaces:**
- Produces: `TransacaoResponseDTO.getStatusRepasse(): StatusRepasse` (nunca null); `FechamentoComissaoDTO.ItemComissaoDTO.getStatusRepasse(): StatusRepasse`.

- [ ] **Step 1: Write the failing test**

```java
// TransacaoResponseDTOTest.java (teste de unidade simples, sem Spring)
@Test
void fromEntityExpoeStatusRepasse() {
    TransacaoFinanceira t = TransacaoFinanceira.builder().descricao("x").valor(BigDecimal.TEN).build();
    // default da entidade é PENDENTE
    TransacaoResponseDTO dto = TransacaoResponseDTO.fromEntity(t);
    assertEquals(StatusRepasse.PENDENTE, dto.getStatusRepasse());
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew test --tests "com.tattos.dto.TransacaoResponseDTOTest"`
Expected: FAIL — `getStatusRepasse()` não existe.

- [ ] **Step 3: Adicionar os campos**

Em `TransacaoResponseDTO`: campo `private StatusRepasse statusRepasse;` (import `com.tattos.model.enums.StatusRepasse`) + linha `.statusRepasse(t.getStatusRepasse())` no `fromEntity`.
Em `FechamentoComissaoDTO.ItemComissaoDTO`: campo `private StatusRepasse statusRepasse;`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew test --tests "com.tattos.dto.TransacaoResponseDTOTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/tattos/dto/response/TransacaoResponseDTO.java src/main/java/com/tattos/dto/response/FechamentoComissaoDTO.java src/test/java/com/tattos/dto/TransacaoResponseDTOTest.java
git commit -m "feat(financeiro): expor statusRepasse nos DTOs de transacao e fechamento"
```

---

### Task 2: Migration V28 — período no pagamento de comissão

**Files:**
- Create: `src/main/resources/db/migration/V28__pagamento_comissao_periodo.sql`
- Modify: `src/main/java/com/tattos/model/PagamentoComissao.java`
- Modify: `src/main/java/com/tattos/dto/response/PagamentoComissaoDTO.java`

**Interfaces:**
- Produces: `PagamentoComissao.getPeriodoInicio()/getPeriodoFim(): LocalDate` (nullable); `PagamentoComissaoDTO.getPeriodoInicio()/getPeriodoFim(): LocalDate`.

- [ ] **Step 1: Criar a migration**

```sql
ALTER TABLE pagamentos_comissao
    ADD COLUMN periodo_inicio DATE NULL,
    ADD COLUMN periodo_fim DATE NULL;
```

(Confirmar o nome real da tabela em `PagamentoComissao.java` — `@Table(name = "pagamentos_comissao")`.)

- [ ] **Step 2: Adicionar campos na entidade e no DTO**

`PagamentoComissao`: `@Column(name = "periodo_inicio") private LocalDate periodoInicio;` e `periodo_fim` idem. `PagamentoComissaoDTO`: campos `LocalDate periodoInicio, periodoFim` + mapeamento no `fromEntity`/mapper existente.

- [ ] **Step 3: Rodar testes existentes do módulo para garantir que nada quebrou**

Run: `./gradlew test --tests "com.tattos.controller.PagamentoComissaoControllerTest"`
Expected: PASS (H2 aplica a migration via Flyway no profile test — se Flyway estiver desligado no profile test, validar com `./gradlew bootRun` local ou teste de contexto).

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/db/migration/V28__pagamento_comissao_periodo.sql src/main/java/com/tattos/model/PagamentoComissao.java src/main/java/com/tattos/dto/response/PagamentoComissaoDTO.java
git commit -m "feat(financeiro): adicionar periodo inicio/fim ao pagamento de comissao"
```

---

### Task 3: Endpoint JSON de fechamento com status por item

**Files:**
- Modify: `src/main/java/com/tattos/controller/ComissaoController.java`
- Modify/Create: `src/main/java/com/tattos/service/ComissaoService.java` (ou onde a lógica de fechamento couber — ver `ComissaoExportService` para reutilizar a query)
- Test: `src/test/java/com/tattos/controller/FechamentoComissaoControllerTest.java`

**Interfaces:**
- Consumes: `TransacaoFinanceiraRepository` (query existente `findTrabalhosParaRepasse` ou similar por tatuador+período), `FechamentoComissaoDTO` com `itens[].statusRepasse` (Task 1).
- Produces: `GET /api/v1/comissoes/tatuador/{tatuadorId}/fechamento?inicio=AAAA-MM-DD&fim=AAAA-MM-DD` → `200 FechamentoComissaoDTO`. Roles: GESTOR (qualquer tatuador), TATUADOR (somente o próprio id — senão 403).

- [ ] **Step 1: Write the failing tests**

`FechamentoComissaoControllerTest` (padrão `PagamentoComissaoControllerTest`):
- `fechamentoRetornaItensComStatusRepasse()` — seed: tatuador + 2 transações (PENDENTE e PAGO); GET como GESTOR; espera 200, `$.itens[*].statusRepasse` contém ambos.
- `fechamentoSemTransacoesRetornaZerado()` — 200, `totalAtendimentos=0`, `itens=[]`.
- `fechamentoComInicioMaiorQueFimRetorna400()`.
- `tatuadorPedindoFechamentoDeOutroRetorna403()` — `@WithMockUser(roles="TATUADOR", username=...)`; ver como outros testes de autorização do repo forçam o usuário logado (`AgendamentoControllerAuthorizationTest`).

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "com.tattos.controller.FechamentoComissaoControllerTest"`
Expected: FAIL — 404 (endpoint não existe).

- [ ] **Step 3: Implementar**

Service: montar `FechamentoComissaoDTO` a partir das transações do tatuador no período (tipo RECEITA com valorRepasse não nulo), preenchendo `statusRepasse` por item e totais (bruto = soma valor, repasse = soma valorRepasse, retido = bruto − repasse). Validar `inicio <= fim` lançando `BusinessException` (mapeada para 400 no `GlobalExceptionHandler` — confirmar nome da exception de validação de negócio existente).
Controller: método `fechamento(...)` com `@PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")` + checagem no service: se logado é TATUADOR e `tatuadorId != logado.getId()` → `AccessDeniedException` (403). Ver como outro controller obtém o usuário logado (ex.: `@AuthenticationPrincipal Usuario`).

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew test --tests "com.tattos.controller.FechamentoComissaoControllerTest"`
Expected: PASS (4 testes)

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat(financeiro): endpoint de fechamento de comissao com status de repasse por item"
```

---

### Task 4: Período no recibo PDF + preenchimento na criação do pagamento

**Files:**
- Modify: `src/main/java/com/tattos/service/ReciboPdfService.java`
- Modify: `src/main/java/com/tattos/service/PagamentoComissaoService.java` (método `criar`)
- Test: `src/test/java/com/tattos/service/ReciboPdfServiceTest.java`

**Interfaces:**
- Consumes: `PagamentoComissao.getPeriodoInicio()/getPeriodoFim()` (Task 2).
- Produces: PDF com linha `<strong>Período:</strong> dd/MM/aaaa – dd/MM/aaaa` (ou `—` quando ambos null).

- [ ] **Step 1: Write the failing test**

```java
@Test
void reciboIncluiPeriodoQuandoInformado() {
    PagamentoComissao p = pagamentoComPeriodo(LocalDate.of(2026,10,1), LocalDate.of(2026,10,31));
    byte[] pdf = service.gerarReciboPagamento(p);
    // extrair texto com PDFBox (dependência já presente via openhtmltopdf) e assert contains "01/10/2026" e "31/10/2026"
}

@Test
void reciboSemPeriodoNaoQuebra() { /* periodo null → gera PDF normalmente, sem linha de período */ }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew test --tests "com.tattos.service.ReciboPdfServiceTest"`
Expected: FAIL — datas não aparecem.

- [ ] **Step 3: Implementar**

`ReciboPdfService.montarHtml`: adicionar linha `Período` no box de cabeçalho quando `periodoInicio != null && periodoFim != null` (formato `dd/MM/yyyy`); caso contrário omitir.
`PagamentoComissaoService.criar`: derivar `periodoInicio = min(data das transações)` e `periodoFim = max(data das transações)` a partir das `transacaoIds` recebidas e setar na entidade antes de salvar.

- [ ] **Step 4: Run tests (novos + módulo financeiro)**

Run: `./gradlew test --tests "com.tattos.service.ReciboPdfServiceTest" --tests "com.tattos.controller.PagamentoComissaoControllerTest"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat(financeiro): incluir periodo do fechamento no recibo PDF"
```

---

### Task 5: Verificação final do módulo

- [ ] **Step 1: Rodar suíte de testes completa**

Run: `./gradlew test`
Expected: PASS sem regressões.

- [ ] **Step 2: Atualizar README (tabela de endpoints), se aplicável**

A linha de Financeiro/Comissões já existe; adicionar menção ao endpoint de fechamento na seção de módulos apenas se houver lista de endpoints — caso contrário, pular.

- [ ] **Step 3: Commit final (se houver mudança)**

```bash
git commit -m "docs: mencionar fechamento de comissao com status de repasse"
```
