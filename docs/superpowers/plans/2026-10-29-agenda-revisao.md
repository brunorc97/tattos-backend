# Agenda — Correção do Filtro e Validações — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fazer a aba Agenda mostrar os agendamentos (hoje ela filtra sempre `data=hoje` no front) e adicionar validações de criação no backend.

**Architecture:** A causa raiz do "não aparece nada" é no **frontend** (`Agenda.jsx` envia `data=hoje` sempre); correção principal lá. No backend: validar passado e grade 09h–19h na criação/atualização, e endpoint consolidado de agenda por período. A auto-confirmação no sinal pago **já existe** (`AgendamentoService.confirmarPagamentoSinal` linhas 133-135) — fora de escopo.

**Tech Stack:** Spring Boot 4 (backend), React (frontend, repo `../tattos-frontend`).

**Spec:** `docs/superpowers/specs/2026-10-29-pacote-evolucoes-design.md` (frente 3)

## Global Constraints

- Backend neste repo; frontend em `/Users/brunocorrea/Projetos - Estudo/tattos-frontend` (commits separados por repo).
- Não alterar formato do `Page<AgendamentoResponseDTO>` consumido pelo front.
- Grade comercial: 09:00–19:00 (alinha com `BancadaService`, slots de 60min).
- Testes backend: padrão repo (`@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`).
- Commits: conventional commits em português.

## Review Focus

1. Agendamento começando exatamente 09:00 ou terminando 19:00 → deve ser **aceito** (bordas inclusivas) — teste Task 1.
2. Atualização (PUT) de agendamento existente para horário inválido → mesma validação da criação — teste Task 1.
3. Endpoint de agenda com `inicio > fim` → 400 — teste Task 2.
4. TATUADOR no endpoint de agenda vê **só a própria** agenda (escopo por role, como `listarComEscopo`) — teste Task 2.
5. Front sem `data` deve enviar `futuras=true` para não listar passado — verificação manual + ajuste em `Agenda.jsx` (Task 3).

---

### Task 1: Validar passado e grade na criação/atualização

**Files:**
- Modify: `src/main/java/com/tattos/service/AgendamentoService.java` (métodos `criar` e `atualizar` — chamar novo `validarJanelaHorario(dataHora, duracaoHoras)`)
- Test: `src/test/java/com/tattos/service/AgendamentoServiceTest.java`

**Interfaces:**
- Produces: `private void validarJanelaHorario(LocalDateTime inicio, BigDecimal duracaoHoras)` — lança `BusinessException` se `inicio.isBefore(LocalDateTime.now())`, se `inicio.getHour() < 9` ou se `inicio.plusMinutes(duracao*60).isAfter(inicio.toLocalDate().atTime(19,0))`.

- [ ] **Step 1: Write the failing tests**

```java
@Test void criarNoPassadoRetornaErro() { /* BusinessException "passado" */ }
@Test void criarForaDaGradeRetornaErro() { /* inicio 08:00 -> erro */ }
@Test void criarTerminandoApos19hRetornaErro() { /* 18:00 + 2h -> erro */ }
@Test void criarNaBordaDaGradeEhAceito() { /* 09:00 + 1h ok; 18:00 + 1h ok */ }
@Test void atualizarParaHorarioInvalidoRetornaErro() { }
```

- [ ] **Step 2: Run to fail** — `./gradlew test --tests "com.tattos.service.AgendamentoServiceTest"` → FAIL.

- [ ] **Step 3: Implementar** `validarJanelaHorario` e chamá-lo em `criar`/`atualizar` antes de `validarConflitos`.

- [ ] **Step 4: Run to pass** + regressão `./gradlew test --tests "com.tattos.service.Agendamento*"` → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(agenda): validar horario passado e grade comercial na criacao/atualizacao"
```

---

### Task 2: Endpoint de agenda consolidada por período

**Files:**
- Modify: `src/main/java/com/tattos/controller/AgendamentoController.java`
- Modify: `src/main/java/com/tattos/service/AgendamentoService.java`
- Modify: `src/main/java/com/tattos/repository/AgendamentoRepository.java` (query por intervalo, se não existir)
- Test: `src/test/java/com/tattos/controller/AgendaVisaoPeriodoTest.java`

**Interfaces:**
- Consumes: `AgendamentoRepository.findComFiltrosECabinePaginado` (já aceita intervalo) ou nova query por intervalo sem paginação.
- Produces: `GET /api/v1/agendamentos/agenda?inicio=AAAA-MM-DD&fim=AAAA-MM-DD` → `List<AgendamentoResponseDTO>` ordenado por `dataHora`. Escopo igual a `listarComEscopo` (TATUADOR → própria agenda; CLIENTE → próprios; GESTOR → tudo). `inicio`/`fim` obrigatórios; `inicio > fim` → 400.

- [ ] **Step 1: Write the failing tests**

```java
@Test void agendaRetornaSomenteDentroDoPeriodo() { }
@Test void agendaComInicioMaiorQueFimRetorna400() { }
@Test void tatuadorVeSomentePropriaAgenda() { }
```

- [ ] **Step 2: Run to fail** → 404.

- [ ] **Step 3: Implementar** — service `agendaPorPeriodo(inicio, fim, logado)` (reutilizar lógica de escopo de `listarComEscopo`, sem paginação, `Sort.by("dataHora")`); validar intervalo; controller com `@PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")`.

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit**

```bash
git commit -am "feat(agenda): endpoint de agenda consolidada por periodo"
```

---

### Task 3: Correção no front — aba Agenda sem filtro de hoje

**Files (repo `../tattos-frontend`):**
- Modify: `src/pages/Agenda.jsx` (~linhas 96, 176-190)

**Interfaces:**
- Consumes: backend aceita `futuras=true` e `data` opcional (já existente).

- [ ] **Step 1: Reproduzir** — subir backend + front, abrir `/agenda` sem sessões hoje: lista vazia; dashboard mostra próximas.

- [ ] **Step 2: Implementar**

Em `Agenda.jsx`: trocar o default de `selectedDate` para vazio/`null` e só incluir `params.data` quando uma data estiver selecionada; quando não houver data, enviar `params.futuras = true`. Garantir que o seletor de data continue funcionando ao escolher um dia específico. Ajustar `src/__tests__/Agenda.test.jsx` se ele fixar a data default.

- [ ] **Step 3: Verificar** — front mostra os próximos agendamentos ao abrir a aba; filtrar um dia continua filtrando.

Run (front): `cd ../tattos-frontend && npm test -- Agenda` (ou o runner do repo).
Expected: PASS.

- [ ] **Step 4: Commit no repo do front**

```bash
cd ../tattos-frontend && git commit -am "fix(agenda): listar proximos agendamentos por padrao em vez de filtrar pelo dia atual"
```

---

### Task 4: Verificação final

- [ ] **Step 1:** `./gradlew test` (backend) — PASS.
- [ ] **Step 2:** Teste manual ponta a ponta: criar agendamento amanhã → aparece na aba Agenda ao abrir; tentar criar ontem → 400 com mensagem clara.
