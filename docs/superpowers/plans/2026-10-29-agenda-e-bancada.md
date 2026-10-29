# Plano 1 — Agenda & Sessões + Bancada

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Visões de agenda por role com filtros, busca de cliente no fluxo de agendamento, fluxo de solicitação do cliente (ideia/imagem/preferências/matching por estilo) e módulo de Bancada (disponibilidade por dia/horário).

**Spec:** `2026-10-29-tattos-spec-v2.md` §6 e §7. Convenções no master plan. **Depende do Plano 0** (upload de arquivos — `arquivoId`; busca de clientes).

**Architecture:** `Agendamento.cabine` (String) já é a "bancada" — disponibilidade = query por cabine+dia, sem entidade nova. Solicitação do cliente = nova entidade `SolicitacaoAgendamento` desacoplada de `Agendamento` (vira agendamento só após aceite do estúdio).

## Review Focus
1. **Cliente forjando `clienteId` de outro** na solicitação → back força o id do token. Teste Task 1.
2. **Matching por estilo sem tatuador com aquele estilo** → lista vazia 200, não erro. Teste Task 3.
3. **Dois agendamentos mesma cabine/horário** → conflito 409 (validação `validarConflitos` já existe — pinar). Teste Task 4.
4. **Filtro de agenda do tatuador ignorando `tatuadorId` alheio** → sempre escopo próprio. Teste Task 2.
5. **Solicitação com imagem >10MB ou não-imagem** → 400 amigável (herda limites do Plano 0 Task 3).

### Task 1: Entidade e endpoints de Solicitação de Agendamento (§6 fluxo do cliente)

**Files:**
- Create: `model/SolicitacaoAgendamento.java`, `repository/SolicitacaoAgendamentoRepository.java`, `controller/SolicitacaoAgendamentoController.java`, `service/SolicitacaoAgendamentoService.java`, `dto/request/SolicitacaoAgendamentoRequestDTO.java`, `dto/response/SolicitacaoAgendamentoResponseDTO.java`
- Create: `src/main/resources/db/migration/V18__solicitacao_agendamento.sql`
- Test: `src/test/java/com/tattos/controller/SolicitacaoAgendamentoControllerTest.java`

**Interfaces:**
- Produces:
  - `POST /api/v1/solicitacoes-agendamento` (CLIENTE) body `{ideia (String, obrig.), imagemReferenciaId (Long arquivoId, opc.), projetoId (Long, opc.), diaPreferencia (LocalDate, opc.), semanaPreferencia (String "2026-W45", opc.), tatuadorId (Long, opc.), estilo (String, opc.)}` → 201 DTO. `clienteId` sempre do token.
  - `GET /api/v1/solicitacoes-agendamento` → CLIENTE: próprias; GESTOR/TATUADOR: todas (GESTOR) ou atribuídas a si/sem tatuador (TATUADOR). Page.
  - `POST /api/v1/solicitacoes-agendamento/{id}/aceitar` (GESTOR,TATUADOR) body `{dataHora, cabine, valor, duracaoMinutos?}` → cria `Agendamento` (status AGENDADO) vinculado, solicitação → `ACEITA`.
  - `POST /api/v1/solicitacoes-agendamento/{id}/recusar` (GESTOR,TATUADOR) body `{motivo}` → status `RECUSADA`.
  - Enum `StatusSolicitacao { PENDENTE, ACEITA, RECUSADA }`.

- [ ] **Step 1: Testes RED** — criar como cliente (201; `clienteId` do token mesmo se body trouxer outro); listar como cliente (só próprias); aceitar como gestor cria agendamento e muda status; recusar com motivo persiste; aceitar solicitação inexistente → 404; aceitar já-aceita → 409.
- [ ] **Step 2: Falhar (404).**
- [ ] **Step 3: Implementar** migration V18 (tabela `solicitacoes_agendamento` com FKs cliente/tatuador/projeto/arquivo + status), entidade, repository, service, controller. Reuso: `AgendamentoService.criar` para o aceite.
- [ ] **Step 4: Verdes + `./gradlew test`.**

### Task 2: Visões e filtros da agenda por role (§6 visão/próximas sessões)

**Files:**
- Modify: `AgendamentoController.java` (listarPaginado), `AgendamentoService.java`, `AgendamentoRepository.java`
- Test: `src/test/java/com/tattos/controller/AgendamentoVisaoTest.java`

**Interfaces:**
- Produces: `GET /api/v1/agendamentos?tatuadorId=&clienteId=&status=&data=&cabine=&futuras=true` → Page. Escopo forçado por role: TATUADOR → sempre próprio id (ignora param); CLIENTE → sempre próprio id; GESTOR → todos com todos os filtros, incl. `cabine`.

- [ ] **Step 1: Testes RED** — tatuador A com `?tatuadorId=B` recebe só as suas; gestor filtra por `cabine=Cabine 01`; `futuras=true` exclui passadas; cliente vê só as dele.
- [ ] **Step 2–4:** falhar → service resolve usuário logado (`SecurityContextHolder`) e aplica escopo; repository ganha `cabine` no `findComFiltrosPaginado` → verdes + suite.

### Task 3: Matching por estilo (§6)

**Files:**
- Modify: `UsuarioController.java` ou novo endpoint em `SolicitacaoAgendamentoController`
- Test: `src/test/java/com/tattos/controller/TatuadorMatchingTest.java`

**Interfaces:**
- Produces: `GET /api/v1/usuarios/tatuadores/sugerir?estilo={texto}` (público autenticado) → lista de tatuadores ativos cujo `especialidade` contém o texto (case-insensitive); sem match → 200 `[]`. Consumido pelo front no passo "escolha por estilo" da solicitação.

- [ ] **Step 1: Teste RED** — seed tatuadores com especialidades "Fineline" e "Old School"; `?estilo=fine` retorna o primeiro; `?estilo=inexistente` → [].
- [ ] **Step 2–4:** falhar → `UsuarioRepository.findByRoleAndEspecialidadeContainingIgnoreCaseAndAtivoTrue` → verde + suite.

### Task 4: Módulo Bancada — disponibilidade (§7)

**Files:**
- Create: endpoint em `AgendamentoController` ou novo `BancadaController.java`
- Test: `src/test/java/com/tattos/controller/BancadaTest.java`

**Interfaces:**
- Produces: `GET /api/v1/bancadas/disponibilidade?cabine={nome}&data={yyyy-MM-dd}` (GESTOR,TATUADOR) → `{cabine, data, horarios: [{inicio, fim, livre: boolean, agendamentoId?}]}` grade 09:00–19:00 em slots de 60min (constante no service). Ocupado = existe agendamento não-cancelado sobreposto.
- Consomes: `AgendamentoRepository` query por cabine+dia.

- [ ] **Step 1: Testes RED** — cabine com agendamento 14:00–16:00 → slots 14h e 15h `livre=false` com `agendamentoId`; cancelado não bloqueia; role CLIENTE → 403.
- [ ] **Step 2–4:** falhar → implementar `BancadaController`+service (lógica de sobreposição: `inicio < agFim && fim > agInicio`) → verdes + suite (inclui pin de conflito 409 ao criar sobreposto via endpoint existente).

### Task 5 (front): telas de agenda, solicitação e bancada

**Files (front):** Modify: `pages/Agenda*.jsx` (ou equivalente), `App.jsx`, `Sidebar.jsx`; Create: `pages/NovaSolicitacao.jsx` (fluxo do cliente: ideia → upload imagem → estilo/tatuador (usa Task 3) → preferências → revisão), `pages/Bancada.jsx` (grade dia/horário por cabine, badge livre/ocupado), lista de solicitações para gestor/tatuador com aceitar/recusar.

- [ ] **Step 1: Testes jest** — Sidebar mostra "Bancada" para TATUADOR/GESTOR; agenda do tatuador chama API sem `tatuadorId`; cliente vê "Nova Solicitação".
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar** telas + rotas + menu; busca de cliente na agenda via `GET /usuarios/clientes?query=` (Plano 0 Task 4); aceitar solicitação abre modal com dataHora/cabine/valor.
- [ ] **Step 4: `npm test -- --watchAll=false`** + smoke manual das 3 roles.
