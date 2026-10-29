# Orçamentos — Fila do Tatuador e Orçamento Rico — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fila de projetos/orçamentos para o tatuador responder com orçamento detalhado (valor fechado ou por sessão), derivando a fila do `Projeto` existente.

**Architecture:** `Projeto` já tem cliente/tatuador/status (`StatusAprovacaoProjeto`) — a fila deriva dele. `Orcamento` (filho de Projeto) ganha campos comerciais: `valorTotal`, `valorSessao`, `sessoesMin/Max`, `tipoCobranca` (FECHADO/POR_SESSAO), `observacoes`, `status` (RASCUNHO/ENVIADO/ACEITO/RECUSADO). Ao ACEITAR orçamento, projeto segue o fluxo de aprovação existente.

**Tech Stack:** Spring Boot 4, Spring Data JPA, Flyway, Bean Validation, JUnit 5 + MockMvc.

**Spec:** `docs/superpowers/specs/2026-10-29-pacote-evolucoes-design.md` (frente 4)

## Global Constraints

- Migrations: próximas livres após V31 (planos anteriores usam V28–V31; este começa em **V32** — renumere se executado fora de ordem).
- Não quebrar `GET/POST /api/v1/orcamentos` atuais (front consome) — mudanças aditivas.
- Ordenação da fila: projetos PENDENTE primeiro, depois por `dataCriacao` ASC.
- TATUADOR vê só projetos/atribuídos a ele; GESTOR vê tudo (filtro `tatuadorId` opcional).
- Testes: padrão repo. Commits: conventional em português.

## Review Focus

1. Orçamento POR_SESSAO sem `valorSessao`/`sessoesMin` (ou FECHADO sem `valorTotal`) → 400 com fieldErrors — teste Task 2.
2. TATUADOR acessando fila vê apenas projetos onde `tatuador.id = logado.id` — teste Task 1.
3. Aceitar orçamento de projeto que não está PENDENTE → 409 — teste Task 3.
4. `sessoesMax < sessoesMin` → 400 — teste Task 2.
5. Listagem antiga `GET /api/v1/orcamentos` continua retornando o que retornava (campos novos aditivos) — regressão Task 2.

---

### Task 1: Endpoint de fila (`GET /api/v1/projetos/fila`)

**Files:**
- Modify: `src/main/java/com/tattos/controller/ProjetoController.java`
- Modify: `src/main/java/com/tattos/service/ProjetoService.java`
- Modify: `src/main/java/com/tattos/repository/ProjetoRepository.java`
- Test: `src/test/java/com/tattos/controller/ProjetoFilaTest.java`

**Interfaces:**
- Produces: `GET /api/v1/projetos/fila?tatuadorId=` → `List<ProjetoResponseDTO>` (DTO existente; confirmar campos) ordenado: `statusAprovacao = PENDENTE` primeiro, depois `dataCriacao` ASC. Roles: TATUADOR (ignora `tatuadorId` alheio — força o próprio), GESTOR (todos ou filtrado).

- [ ] **Step 1: Write the failing tests**

```java
@Test void filaTatuadorRetornaSomenteSeusProjetos() { }
@Test void filaOrdenaPendentesPrimeiroDepoisMaisAntigos() { }
@Test void filaGestorFiltraPorTatuadorId() { }
@Test void clienteNaoAcessaFilaRetorna403() { }
```

- [ ] **Step 2: Run to fail** — `./gradlew test --tests "com.tattos.controller.ProjetoFilaTest"` → 404.

- [ ] **Step 3: Implementar** — repository query com ordenação (`ORDER BY CASE WHEN status_aprovacao = 'PENDENTE' THEN 0 ELSE 1 END, data_criacao ASC` via @Query); service com escopo por role (padrão `listarComEscopo` de AgendamentoService); endpoint `@PreAuthorize("hasAnyRole('GESTOR','TATUADOR')")`.

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(orcamento): fila de projetos para tatuador com escopo por role"
```

---

### Task 2: Campos comerciais no Orcamento

**Files:**
- Create: `src/main/resources/db/migration/V32__orcamento_comercial.sql`
- Create: `src/main/java/com/tattos/model/enums/TipoCobranca.java` (`FECHADO, POR_SESSAO`) e `StatusOrcamento.java` (`RASCUNHO, ENVIADO, ACEITO, RECUSADO`)
- Modify: `src/main/java/com/tattos/model/Orcamento.java`
- Modify: `src/main/java/com/tattos/dto/request/OrcamentoRequestDTO.java` e `src/main/java/com/tattos/dto/response/OrcamentoResponseDTO.java`
- Modify: `src/main/java/com/tattos/service/OrcamentoService.java` (validação cruzada)
- Test: `src/test/java/com/tattos/controller/OrcamentoControllerTest.java` (estender)

**Interfaces:**
- Produces: `Orcamento` + campos `valorTotal`, `valorSessao`, `sessoesMin`, `sessoesMax`, `tipoCobranca` (STRING), `observacoes`, `status` (STRING, default RASCUNHO). Regras: FECHADO ⇒ `valorTotal` obrigatório; POR_SESSAO ⇒ `valorSessao` e `sessoesMin` obrigatórios e `sessoesMax >= sessoesMin` quando presente.

- [ ] **Step 1: Write the failing tests** — validações cruzadas acima (400 com `fieldErrors` ou BusinessException conforme padrão do repo); criação FECHADA válida retorna 201 com campos; regressão do payload antigo `{nome, valor}`.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar** — migration (ADD COLUMNs), enums, entidade, DTOs (aditivo), validação no service (`@Validated` custom ou checagem manual lançando `BusinessException`; preferir validação manual pois é cruzada).

- [ ] **Step 4: Run to pass** + `./gradlew test --tests "com.tattos.controller.Orcamento*"` → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(orcamento): campos comerciais (valor fechado ou por sessao) e status"
```

---

### Task 3: Ciclo de vida do orçamento (enviar/aceitar/recusar)

**Files:**
- Modify: `src/main/java/com/tattos/controller/OrcamentoController.java`
- Modify: `src/main/java/com/tattos/service/OrcamentoService.java`
- Test: `src/test/java/com/tattos/controller/OrcamentoStatusTest.java`

**Interfaces:**
- Consumes: `StatusOrcamento` (Task 2), `Projeto.statusAprovacao` existente.
- Produces:
  - `PATCH /api/v1/orcamentos/{id}/status` body `{"status": "ENVIADO|ACEITO|RECUSADO"}`.
  - Transições: RASCUNHO→ENVIADO (GESTOR/TATUADOR dono), ENVIADO→ACEITO|RECUSADO (CLIENTE dono do projeto ou GESTOR). ACEITO ⇒ `projeto.statusAprovacao = APROVADO`. Fora da transição válida → 409.

- [ ] **Step 1: Write the failing tests** — transições válidas e inválidas (409), CLIENTE de outro projeto → 403, aceitar reflete no projeto.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar** máquina de estados no service + endpoint.

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit + suíte**

```bash
git commit -am "feat(orcamento): ciclo de vida enviar/aceitar/recusar integrado ao projeto"
./gradlew test  # PASS
```
