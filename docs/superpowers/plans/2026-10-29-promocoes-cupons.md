# Plano 6 — Promoções e Cupons

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Cupons de desconto criados por gestor/tatuador, com abrangência (studio inteiro ou um tatuador), tipo porcentagem ou valor fixo, aplicáveis no agendamento — separados do código de indicação existente.

**Spec:** `2026-10-29-tattos-spec-v2.md` §10. **Depende do Plano 1** (agenda: aplicação do desconto no agendamento). Independe dos demais.

**Architecture:** Nova entidade `Cupom` — distinta de `codigoIndicacao` (fidelização). Aplicação no `Agendamento` via `codigoCupom` opcional no request; desconto consolidado no `descontoAplicado` já existente (soma indicação + cupom, nunca negativo).

## Review Focus
1. **Cupom de tatuador A aplicado em agendamento do tatuador B** → 400/409 (abrangência violada). Teste Task 2.
2. **Cupom expirado ou esgotado (limite de usos)** → 400 com mensagem clara. Teste Task 2.
3. **Desconto fixo maior que o valor** → desconto truncado no valor, nunca total negativo. Teste Task 2.
4. **Código duplicado** → 409. Teste Task 1.
5. **Cupom + indicação juntos** → soma limitada ao valor do procedimento. Teste Task 2.

### Task 1: CRUD de cupons

**Files:**
- Create: `model/Cupom.java`, repository, `controller/CupomController.java`, service, DTOs, `V26__cupons.sql`
- Test: `src/test/java/com/tattos/controller/CupomControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/cupons` (GESTOR,TATUADOR) body `{codigo (único, upper), tipo (PORCENTAGEM|VALOR_FIXO), valor BigDecimal, tatuadorId? (null = todo o studio), validade?, limiteUsos?, ativo=true}` → 201. Tatuador só cria cupom para si (`tatuadorId` forçado ao próprio; tentar criar para outro → 403). `GET /api/v1/cupons?ativo=true` (GESTOR: todos; TATUADOR: próprios + studio). `PATCH /{id}/desativar`.

- [ ] **Step 1: Testes RED** — criar como gestor (201, studio-wide); tatuador criando para outro → 403; código duplicado → 409; listar como tatuador (próprios + studio, não os de outro tatuador).
- [ ] **Step 2–4:** falhar → migration + CRUD → verdes + suite.

### Task 2: Aplicação no agendamento

**Files:**
- Modify: `dto/request/AgendamentoRequestDTO.java` (+`codigoCupom?`), `AgendamentoService.criar/atualizar`, `CupomService` (método `validarECalcular`)
- Test: `src/test/java/com/tattos/service/AgendamentoServiceTest.java` (estender), `CupomControllerTest`

**Interfaces:**
- Consumes: `Cupom` (Task 1); `descontoAplicado`/`calcularDescontoIndicacao` existentes em `AgendamentoService`.
- Produces: `CupomService.validarECalcular(codigo, tatuadorId, valorBase): BigDecimal` — valida ativo, validade, limite de usos (`usos < limiteUsos`), abrangência (studio ou tatuador do agendamento); retorna desconto (porcentagem de valorBase ou min(fixo, valorBase)). `AgendamentoService` soma ao `descontoAplicado` com teto no valor; incrementa `usos` do cupom no commit (via mesmo transaction).

- [ ] **Step 1: Testes RED** — cupom 10% em valor 1000 → total líquido 900; fixo R$200 em valor 150 → desconto 150 (total 0); cupom de A em agendamento de B → 400; expirado → 400; esgotado → 400; cupom 10% + indicação 10% em 1000 → desconto 200.
- [ ] **Step 2–4:** falhar → implementar → verdes + suite.

### Task 3 (front): telas de cupons

**Files (front):** Create: `pages/Cupons.jsx` (lista + criação; tatuador sem seletor de tatuador); Modify: fluxo de agendamento (Plano 1) com campo "cupom" e preview do desconto; `App.jsx`/`Sidebar.jsx` (item "Promoções", GESTOR+TATUADOR).

- [ ] **Step 1: Testes jest** — menu por role; aplicar cupom inválido exibe `message` do 400.
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar.**
- [ ] **Step 4: `npm test`** + smoke manual.
