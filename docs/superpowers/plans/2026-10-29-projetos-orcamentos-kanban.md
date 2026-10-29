# Plano 2 — Projetos, Orçamentos e Kanban

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Projeto vinculado a cliente e tatuador, com prazo, aprovação/recusa com motivo, controle de sinal com comprovante, flag de atraso no Kanban e permissões por role.

**Spec:** `2026-10-29-tattos-spec-v2.md` §3. Convenções no master plan. **Depende do Plano 0** (upload — comprovante PDF; busca de clientes).

**Architecture:** Estender a entidade `Projeto` existente (hoje: `id, nome, descricao, dataCriacao, aprovado:boolean`) em vez de criar módulo novo — o spec diz "se já tiver módulo parecido não precisa criar nada". `boolean aprovado` vira enum `StatusAprovacaoProjeto { PENDENTE, APROVADO, NAO_APROVADO }` (migration converte dados: `aprovado=true`→APROVADO, false→PENDENTE).

## Review Focus
1. **Recusa sem motivo** → 400 (motivo obrigatório quando NAO_APROVADO). Teste Task 2.
2. **Tatuador listando projetos** → só os dele; param `tatuadorId` alheio ignorado. Teste Task 3.
3. **Comprovante não-PDF ou >10MB** → 400 (limites do Plano 0). Teste Task 4.
4. **Prazo no passado + status não concluído no Kanban** → `atrasado=true` calculado, não armazenado (evita dessincronia). Teste Task 5.
5. **Sinal pago maior que o valor do projeto** → 400 de regra de negócio. Teste Task 4.

### Task 1: Migration e modelo — Projeto estendido

**Files:**
- Create: `src/main/resources/db/migration/V19__projeto_cliente_prazo_aprovacao.sql`
- Modify: `model/Projeto.java`, `model/enums/` (novo `StatusAprovacaoProjeto`, `MotivoRecusaProjeto`)
- Test: `src/test/java/com/tattos/service/ProjetoServiceTest.java` (novo ou estender existente)

**Interfaces:**
- Produces: `Projeto` ganha: `@ManyToOne Usuario cliente` (nullable=false p/ novos; migration deixa NULL nos legados), `@ManyToOne Usuario tatuador` (nullable), `LocalDate prazo`, `StatusAprovacaoProjeto statusAprovacao` (default PENDENTE), `MotivoRecusaProjeto motivoRecusa` ({VALOR, NAO_GOSTOU_DESENHO, DATA_ADIADA, OUTRO} + `String motivoRecusaDetalhe`), `boolean necessitaSinal`, `BigDecimal valorSinal`, `boolean sinalPago`, `LocalDateTime dataPagamentoSinal`, `String formaPagamentoSinal`, `@ManyToOne Arquivo comprovanteSinal`. Mantém `aprovado` deprecado mapeado para o enum (getter derivado) para não quebrar KanbanCard/Orçamento existentes.

- [ ] **Step 1: Teste RED** — salvar Projeto com cliente/tatuador/prazo e ler de volta; legado `aprovado=true` lê APROVADO.
- [ ] **Step 2–4:** falhar → migration V19 (ADD COLUMNs + `UPDATE projetos SET status_aprovacao='APROVADO' WHERE aprovado=true` + drop da coluna boolean antiga ao final) + entidade → verdes + suite.

### Task 2: Aprovar / Recusar com motivo

**Files:**
- Modify: `ProjetoController.java`, `ProjetoService.java`, `dto/request/AprovacaoProjetoRequestDTO.java` (novo)
- Test: `ProjetoControllerTest` (estender existente)

**Interfaces:**
- Produces: `POST /api/v1/projetos/{id}/decisao` (GESTOR,TATUADOR) body `{aprovado: boolean, motivoRecusa?: MotivoRecusaProjeto, detalhe?: String}` → 200 DTO. Recusa sem motivo → 400. Mantém legado `POST /{id}/aprovar` delegando (deprecated).

- [ ] **Step 1: Testes RED** — aprovar → APROVADO; recusar com VALOR+detalhe → NAO_APROVADO persistido; recusar sem motivo → 400 `fieldErrors.motivoRecusa`; inexistente → 404 (já coberto por ResourceNotFoundException do hardening).
- [ ] **Step 2–4:** falhar → implementar → verdes + suite.

### Task 3: Vínculo e permissões de listagem

**Files:**
- Modify: `ProjetoController.listar`, `ProjetoService`, `ProjetoRepository`, `dto/request/ProjetoRequestDTO.java`
- Test: `ProjetoControllerTest`

**Interfaces:**
- Produces: `POST /projetos` exige `clienteId` (400 sem), aceita `tatuadorId?`, `prazo?`. `GET /projetos?tatuadorId=&statusAprovacao=&clienteId=` → Page; TATUADOR → só os seus; GESTOR → todos com filtros; CLIENTE → só os dele.

- [ ] **Step 1: Testes RED** — criar sem clienteId → 400; tatuador A não vê projeto do tatuador B; gestor filtra `statusAprovacao=NAO_APROVADO`.
- [ ] **Step 2–4:** falhar → implementar (paginação — seguir padrão do hardening) → verdes + suite.

### Task 4: Sinal do projeto com comprovante

**Files:**
- Modify: `ProjetoController`, `ProjetoService`
- Test: `ProjetoControllerTest`

**Interfaces:**
- Consumes: `POST /api/v1/files` do Plano 0 (Task 3) → `arquivoId`.
- Produces: `PATCH /api/v1/projetos/{id}/sinal` (GESTOR,TATUADOR) body `{necessitaSinal, valorSinal?}`; `POST /api/v1/projetos/{id}/sinal/comprovante` (CLIENTE dono) body `{arquivoId}` → anexa comprovante; `POST /api/v1/projetos/{id}/sinal/confirmar` (GESTOR,TATUADOR) body `{formaPagamento}` → marca pago + `dataPagamentoSinal`. Regra: `valorSinal > valor do orçamento vinculado` (se houver) → 400.

- [ ] **Step 1: Testes RED** — cliente anexa PDF (200); cliente de outro projeto → 403; confirmar sem comprovante ok (200, gestor dá entrada manual); sinal > valor → 400.
- [ ] **Step 2–4:** falhar → implementar → verdes + suite.

### Task 5: Flag de atraso no Kanban

**Files:**
- Modify: `dto/response/KanbanCardResponseDTO.java`, `service/KanbanCardService.java`
- Test: `src/test/java/com/tattos/service/KanbanAtrasoTest.java`

**Interfaces:**
- Consumes: `Projeto.prazo` (Task 1).
- Produces: `KanbanCardResponseDTO` ganha `Boolean atrasado` — true quando `projeto.prazo < hoje` e `card.status` não é status final ("CONCLUIDO"/"DONE" — lista de finais em constante no service). Calculado na leitura.

- [ ] **Step 1: Teste RED** — card vinculado a projeto com prazo ontem + status "EM_ANDAMENTO" → `atrasado=true`; mesmo prazo com status "CONCLUIDO" → false.
- [ ] **Step 2–4:** falhar → implementar → verdes + suite.

### Task 6 (front): telas de projetos/orçamentos

**Files (front):** Modify: `pages/Projetos*.jsx`/`Orcamento.jsx`, `components/KanbanBoard.jsx`, `ProjectApprovalModal.jsx`; Create: modal/tela de decisão com motivo de recusa, formulário de projeto com cliente (busca do Plano 0 Task 4), seção de sinal com upload PDF (Plano 0 Task 3), badge "Atrasado" no card do Kanban.

- [ ] **Step 1: Testes jest** — badge atrasado renderiza; modal de recusa exige motivo; tatuador não vê filtro de tatuador.
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar** telas + integrações dos endpoints das Tasks 2–5.
- [ ] **Step 4: `npm test`** + smoke manual.
