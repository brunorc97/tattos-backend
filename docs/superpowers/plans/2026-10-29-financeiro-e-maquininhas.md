# Plano 4 — Financeiro & Máquinas de Cartão

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Separação estrita receita×despesa, cadastro de taxas por operadora/modalidade com cálculo de valor líquido, detalhes de pagamento e dashboard interativo.

**Spec:** `2026-10-29-tattos-spec-v2.md` §5. Independente dos planos 1–3 (só Plano 0).

**Architecture:** Nova entidade `TaxaOperadora` (operadora + modalidade + taxa %). `TransacaoFinanceira.formaPagamento` (String livre hoje) permanece, mas receitas ganham vínculo opcional `taxaOperadoraId` → colunas calculadas `taxaValor` e `valorLiquido` persistidas na criação. ADIANTAMENTO sai do balanço (não é receita nem despesa).

## Review Focus
1. **Taxa de crédito parcelado aplicada a débito** (modalidade errada) → o cálculo usa a taxa da modalidade informada, nunca outra. Teste Task 2.
2. **Balanço misturando ADIANTAMENTO como receita** → excluído de receitas/despesas. Teste Task 3.
3. **Editar taxa retroativamente** → não recalcula transações passadas (valores persistidos). Teste Task 1.
4. **Transação em dinheiro/PIX sem operadora** → `valorLiquido = valor`. Teste Task 2.
5. **Dashboard: clique em "Despesas"** mostra só despesas (interatividade real, não mock). Teste front Task 4.

### Task 1: Cadastro de taxas por operadora

**Files:**
- Create: `model/TaxaOperadora.java`, repository, `controller/TaxaOperadoraController.java`, service, DTOs, `V22__taxa_operadora.sql`
- Test: `src/test/java/com/tattos/controller/TaxaOperadoraControllerTest.java`

**Interfaces:**
- Produces: CRUD `POST/GET/PUT/DELETE /api/v1/financeiro/taxas-operadoras` (GESTOR). Campos: `operadora` (String, ex. "Stone"), `modalidade` enum `ModalidadePagamento { DEBITO, CREDITO_A_VISTA, CREDITO_PARCELADO }`, `taxaPercentual BigDecimal(0–100)`, `ativo`. Unicidade (operadora, modalidade) → 409 duplicado. Edição não recalcula histórico.

- [ ] **Step 1: Testes RED** — criar (201), duplicado (409), listar só ativas, editar percentual não altera transação criada antes (pin: criar taxa 2% → transação → editar taxa 5% → transação antiga mantém 2%).
- [ ] **Step 2–4:** falhar → migration + CRUD → verdes + suite.

### Task 2: Valor líquido na transação

**Files:**
- Modify: `model/TransacaoFinanceira.java`, `service/TransacaoService.java`, DTOs
- Create: `V23__transacao_taxa_e_liquido.sql`
- Test: `src/test/java/com/tattos/service/TransacaoServiceTest.java` (estender)

**Interfaces:**
- Consumes: `TaxaOperadora` (Task 1).
- Produces: `TransacaoRequestDTO` ganha `taxaOperadoraId?`; entidade ganha `@ManyToOne TaxaOperadora taxaOperadora`, `taxaValor BigDecimal`, `valorLiquido BigDecimal`. No `criar` de RECEITA com taxa: `taxaValor = valor * taxa/100`, `valorLiquido = valor - taxaValor`; sem taxa → taxaValor 0, liquido = valor. DESPESA/ADIANTAMENTO ignoram taxa. Response expõe os novos campos.

- [ ] **Step 1: Testes RED** — receita R$1000 com taxa débito 1,99% → taxa 19,90 / líquido 980,10; receita PIX sem operadora → líquido=valor; despesa com taxaOperadoraId → 400.
- [ ] **Step 2–4:** falhar → migration + cálculo no service → verdes + suite.

### Task 3: Balanço estrito + detalhes de pagamento

**Files:**
- Modify: `TransacaoService.obterBalancoMensal`, `BalancoFinanceiroDTO`, repository queries
- Test: `TransacaoServiceTest`, `TransacaoControllerTest`

**Interfaces:**
- Produces: `GET /financeiro/balanco` → adiciona `totalTaxas`, `faturamentoLiquido`, e **exclui ADIANTAMENTO** de receitas/despesas (pin com teste). `GET /transacoes/{id}` retorna detalhes completos de pagamento (formaPagamento, taxa, líquido, operadora).

- [ ] **Step 1: Testes RED** — balanço com 1 RECEITA+taxa, 1 DESPESA, 1 ADIANTAMENTO → receita bruta/líquida/taxas corretos e adiantamento fora.
- [ ] **Step 2–4:** falhar → ajustar queries (`sumReceitasByPeriodo` etc. ganham filtro de tipo) → verdes + suite.

### Task 4 (front): dashboard interativo + taxas

**Files (front):** Modify: `pages/FinancialDashboard.jsx` (gráficos recharts reais, sem mock/"Aerografia" — coordenar com Plano 0 Task 6), drill-down: clicar em Receita/Despesa alterna painel de detalhes com a lista filtrada; Create: `pages/TaxasOperadoras.jsx` (CRUD GESTOR).

- [ ] **Step 1: Testes jest** — clique em Despesas renderiza lista de despesas (mock api); aba taxas restrita a GESTOR.
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar** interatividade + CRUD de taxas + exibição de líquido/taxa nas linhas de transação.
- [ ] **Step 4: `npm test`** + smoke manual.
