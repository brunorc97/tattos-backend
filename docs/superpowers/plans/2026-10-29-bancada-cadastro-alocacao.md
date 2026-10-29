# Bancada — Cadastro e Alocação por Horário — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transformar bancada em entidade cadastrável (GESTOR) e permitir alocação por horário via agendamento referenciando `bancada_id`, mantendo compat com o campo String `cabine`.

**Architecture:** Nova entidade `Bancada` + CRUD + migration V30. `Agendamento` ganha `bancada` (ManyToOne, nullable no início) com backfill a partir de `cabine` (match por nome). Conflito de espaço e grade de disponibilidade passam a usar `bancada_id` quando presente, com fallback para `cabine` (transição).

**Tech Stack:** Spring Boot 4, Spring Data JPA, Flyway, Bean Validation, JUnit 5 + MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-29-pacote-evolucoes-design.md` (frente 6)

## Global Constraints

- Migration: **V30** (após V29 do plano de estoque; se executado antes, renumere para a próxima livre).
- Compat: `cabine` (String) permanece no DTO e na entidade nesta versão; `bancadaId` é aditivo.
- Alocação de bancada acontece pelo agendamento (sem entidade de reserva paralela — decisão do design).
- Roles: CRUD de bancada = GESTOR; alocar (criar agendamento com bancada) = GESTOR e TATUADOR; disponibilidade = GESTOR e TATUADOR (como hoje).
- Testes: padrão repo. Commits: conventional em português.

## Review Focus

1. Dois agendamentos na mesma bancada/horário → conflito 409 mesmo usando `bancadaId` (hoje o conflito usa String `cabine`) — teste Task 3.
2. Agendamento com `bancadaId` inexistente ou bancada inativa → 400/422 — teste Task 3.
3. Backfill: `cabine` sem bancada de mesmo nome → `bancada_id` fica null e o sistema continua funcionando pelo fallback — teste Task 2 (migration) / Task 3 (fallback).
4. Deletar bancada com agendamentos futuros → recusar (BusinessException) em vez de quebrar FK — teste Task 1.
5. Grade de disponibilidade por `bancadaId` não pode mostrar slots de outra bancada — teste Task 4.

---

### Task 1: Entidade Bancada + migration + CRUD

**Files:**
- Create: `src/main/resources/db/migration/V30__bancada.sql`
- Create: `src/main/java/com/tattos/model/Bancada.java`
- Create: `src/main/java/com/tattos/repository/BancadaRepository.java`
- Create: `src/main/java/com/tattos/service/BancadaCrudService.java` (manter `BancadaService` para disponibilidade)
- Create: `src/main/java/com/tattos/dto/request/BancadaRequestDTO.java`, `src/main/java/com/tattos/dto/response/BancadaResponseDTO.java`
- Modify: `src/main/java/com/tattos/controller/BancadaController.java`
- Test: `src/test/java/com/tattos/controller/BancadaCrudTest.java`

**Interfaces:**
- Produces:
  - `Bancada { id, nome (unique, NotBlank), descricao, ativa (default true) }`
  - Endpoints: `GET /api/v1/bancadas` (GESTOR/TATUADOR), `POST /api/v1/bancadas`, `PUT /api/v1/bancadas/{id}`, `DELETE /api/v1/bancadas/{id}` (GESTOR; DELETE recusa se houver agendamento futuro não cancelado na bancada).
  - `BancadaResponseDTO { id, nome, descricao, ativa }`

- [ ] **Step 1: Write the failing tests** — `BancadaCrudTest`: criar/listar/atualizar/deletar; nome duplicado → 409/400; TATUADOR não cria (403); DELETE com agendamento futuro → erro de negócio.

- [ ] **Step 2: Run to fail** → 404.

- [ ] **Step 3: Implementar** migration (`bancada` table: id, nome unique, descricao, ativa), entidade, repositório, service e endpoints no `BancadaController` existente.

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(bancada): cadastro de bancadas com CRUD para gestor"
```

---

### Task 2: Agendamento.bancada + backfill

**Files:**
- Create: `src/main/resources/db/migration/V31__agendamento_bancada.sql`
- Modify: `src/main/java/com/tattos/model/Agendamento.java`
- Modify: `src/main/java/com/tattos/dto/request/AgendamentoRequestDTO.java` (add `@Schema(example="1") private Long bancadaId;`)
- Modify: `src/main/java/com/tattos/dto/response/AgendamentoResponseDTO.java` (add `bancadaId`, `bancadaNome`)
- Test: `src/test/java/com/tattos/controller/AgendamentoVisaoTest.java` (estender) ou novo `AgendamentoBancadaTest`

**Interfaces:**
- Produces: `Agendamento.getBancada(): Bancada` (nullable); DTOs com `bancadaId`/`bancadaNome` aditivos.

- [ ] **Step 1: Migration**

```sql
ALTER TABLE agendamentos ADD COLUMN bancada_id BIGINT NULL,
    ADD CONSTRAINT fk_agendamento_bancada FOREIGN KEY (bancada_id) REFERENCES bancada(id);
-- backfill por nome (MySQL):
UPDATE agendamentos a JOIN bancada b ON b.nome = a.cabine SET a.bancada_id = b.id;
```

(Confirmar nome da tabela de agendamentos na entidade.)

- [ ] **Step 2: Write failing test** — criar agendamento com `bancadaId` persiste e resposta traz `bancadaId`/`bancadaNome`; criar com `bancadaId` inexistente → 400/422.

- [ ] **Step 3: Implementar** mapeamento na entidade, campos nos DTOs, e no `AgendamentoService.criar/atualizar`: se `bancadaId` presente, resolver bancada (ativa — senão BusinessException) e sincronizar `cabine = bancada.getNome()` para compat; se ausente, seguir como hoje.

- [ ] **Step 4: Run to pass** + regressão `./gradlew test --tests "com.tattos.controller.Agendamento*"` → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(bancada): agendamento referencia bancada com backfill e compat"
```

---

### Task 3: Conflito de espaço por bancada

**Files:**
- Modify: `src/main/java/com/tattos/repository/AgendamentoRepository.java` (`existeConflitoCabine` → sobrecarga por bancadaId)
- Modify: `src/main/java/com/tattos/service/AgendamentoService.java` (`validarConflitos`)
- Test: `src/test/java/com/tattos/service/AgendamentoServiceTest.java`

**Interfaces:**
- Consumes: `Agendamento.getBancada()` (Task 2).
- Produces: `AgendamentoRepository.existeConflitoBancada(Long bancadaId, LocalDateTime inicio, LocalDateTime fim, Long ignorarId): boolean`.

- [ ] **Step 1: Write failing tests** — conflito por mesma bancada/horário → 409; mesmo horário em bancadas diferentes → ok; agendamento legado (só `cabine`) ainda valida pelo fallback String.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar** — em `validarConflitos`: se `agendamento.getBancada() != null` usar `existeConflitoBancada`, senão `existeConflitoCabine` (atual).

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(bancada): validar conflito de espaco por bancada"
```

---

### Task 4: Disponibilidade por bancadaId

**Files:**
- Modify: `src/main/java/com/tattos/service/BancadaService.java`
- Modify: `src/main/java/com/tattos/controller/BancadaController.java`
- Test: `src/test/java/com/tattos/controller/BancadaTest.java` (estender)

**Interfaces:**
- Produces: `GET /api/v1/bancadas/disponibilidade?bancadaId=1&data=AAAA-MM-DD` (novo, preferencial) — manter `?cabine=` funcionando (deprecated no OpenAPI). Mesmo formato de grade (Map com `horarios` livre/ocupado), acrescentando `bancadaId`/`bancadaNome` no retorno.

- [ ] **Step 1: Write failing tests** — disponibilidade por `bancadaId` mostra slot ocupado só daquela bancada; chamada antiga por `cabine` continua funcionando.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar** — `disponibilidade(Long bancadaId, LocalDate data)`: filtra por `a.getBancada().getId()`; fallback por nome quando agendamento legado sem bancada.

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit + suíte final**

```bash
git commit -am "feat(bancada): disponibilidade por bancadaId mantendo compat com cabine"
./gradlew test  # suíte completa PASS
```
