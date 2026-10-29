# Plano 5 — Comissões, Pagamentos, Adiantamentos e Recibos

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Fluxo completo de pagamento de comissões (seleção de trabalhos → pagamento agendado → pago com comprovante/recibo PDF), adiantamentos com abatimento automático, histórico, e visão do próprio tatuador.

**Spec:** `2026-10-29-tattos-spec-v2.md` §9. **Depende dos Planos 0** (upload, dados bancários em Usuario) **e 4** (financeiro).

**Architecture:** `TipoTransacao.ADIANTAMENTO` já existe no enum — vira entidade de primeira classe. Novo agregado `PagamentoComissao` (cabeçalho) com itens (transações selecionadas). Status de recebimento por transação: nova coluna `statusRepasse` em `transacoes_financeiras` (`PENDENTE, AGENDADO, PAGO`). Recibo PDF gerado server-side com **OpenHTMLtoPDF** (lib leve, sem headless browser) e salvo via building block de arquivos (Plano 0).

## Review Focus
1. **Duplo pagamento da mesma transação** → selecionar transação já PAGO/AGENDADO em novo pagamento → 409. Teste Task 2.
2. **Adiantamento maior que o total selecionado** → abate até zerar; sobra de adiantamento permanece em aberto (nunca valor negativo a pagar). Teste Task 3.
3. **Tatuador vendo repasses de outro** → escopo forçado ao próprio id. Teste Task 5.
4. **Recibo PDF de pagamento inexistente** → 404. Teste Task 4.
5. **Pagamento em dinheiro sem recibo anexado** → permitido (recibo pode ser gerado pelo sistema); Pix sem comprovante → permitido também, mas `comprovanteId` registrado quando enviado. Sem bloqueio artificial — pin de flexibilidade no Teste Task 2.

### Task 1: Adiantamentos (módulo dedicado)

**Files:**
- Create: `controller/AdiantamentoController.java`, service, DTOs
- Modify: `model/TransacaoFinanceira.java` (ou reutilizar com tipo ADIANTAMENTO + colunas), `V24__adiantamentos.sql`
- Test: `src/test/java/com/tattos/controller/AdiantamentoControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/financeiro/adiantamentos` (GESTOR) body `{tatuadorId, valor, data?, reciboId? (arquivoId), observacao?}` → 201 (cria TransacaoFinanceira tipo ADIANTAMENTO + coluna `abatido BigDecimal default 0`). `GET /api/v1/financeiro/adiantamentos?tatuadorId=&emAberto=true` → Page. `saldoEmAberto(tatuadorId): BigDecimal` = Σ(valor) − Σ(abatido) — método público do service consumido pela Task 3.

- [ ] **Step 1: Testes RED** — criar (201) e saldo em aberto; dois adiantamentos somam; sem auth GESTOR → 403 para TATUADOR criando.
- [ ] **Step 2–4:** falhar → migration + endpoints → verdes + suite.

### Task 2: Pagamento de comissões (seleção → agendado → pago)

**Files:**
- Create: `model/PagamentoComissao.java`, `model/PagamentoComissaoItem.java`, repositories, `controller/PagamentoComissaoController.java`, service, DTOs, `V25__pagamento_comissao.sql`
- Modify: `TransacaoFinanceira` + coluna `status_repasse` (`V25`)
- Test: `src/test/java/com/tattos/controller/PagamentoComissaoControllerTest.java`

**Interfaces:**
- Consumes: dados bancários de `Usuario` (Plano 0 Task 1: `pixChave/banco/agencia/conta`), upload (Plano 0 Task 3).
- Produces:
  - `GET /api/v1/financeiro/comissoes/{tatuadorId}/trabalhos?inicio=&fim=&statusRepasse=PENDENTE` (GESTOR) → Page de transações com `valorRepasse`.
  - `POST /api/v1/financeiro/pagamentos-comissao` (GESTOR) body `{tatuadorId, transacaoIds: [Long], formaPagamento (PIX|DINHEIRO), comprovanteId?}` → 201 `PagamentoComissaoDTO {id, tatuadorId, totalBruto, totalAdiantamentosAbatidos, totalLiquido, status: AGENDADO, itens:[...]}`. Valida: toda transação é RECEITA do tatuador e `statusRepasse=PENDENTE` (senão 409); marca itens como AGENDADO; abate adiantamentos (Task 3).
  - `POST /api/v1/financeiro/pagamentos-comissao/{id}/confirmar` (GESTOR) → status PAGO, itens → PAGO.
  - `GET /api/v1/financeiro/pagamentos-comissao?tatuadorId=&status=` (GESTOR) → histórico Page.

- [ ] **Step 1: Testes RED** — fluxo feliz (selecionar 2 trabalhos → AGENDADO → confirmar → PAGO); re-selecionar transação AGENDADA → 409; transação de outro tatuador → 400.
- [ ] **Step 2–4:** falhar → migrations + agregado + endpoints → verdes + suite.

### Task 3: Abatimento automático de adiantamentos

**Files:**
- Modify: service de `PagamentoComissao` (Task 2), `AdiantamentoService`
- Test: `PagamentoComissaoControllerTest`

**Interfaces:**
- Consumes: `AdiantamentoService.saldoEmAberto(tatuadorId)` (Task 1).
- Produces: no `POST /pagamentos-comissao`: `abatido = min(saldoEmAberto, totalBruto)`; adiantamentos mais antigos abatidos primeiro (FIFO, campo `abatido` incrementado); `totalLiquido = totalBruto - abatido`; se saldo > total, sobra continua em aberto.

- [ ] **Step 1: Testes RED** — total 1000 com adiantamento 300 → líquido 700; adiantamento 1500 em total 1000 → líquido 0 e saldo 500 restante; sem adiantamento → líquido=bruto.
- [ ] **Step 2–4:** falhar → FIFO no service → verdes + suite.

### Task 4: Recibo em PDF

**Files:**
- Create: `service/ReciboPdfService.java`, template HTML simples (string ou classpath), endpoint no `PagamentoComissaoController`
- Modify: `build.gradle` (`com.openhtmltopdf:openhtmltopdf-pdfbox`)
- Test: `src/test/java/com/tattos/service/ReciboPdfServiceTest.java`

**Interfaces:**
- Produces: `GET /api/v1/financeiro/pagamentos-comissao/{id}/recibo.pdf` (GESTOR, TATUADOR dono) → `application/pdf` com itens, abatimentos, líquido, dados bancários; ao confirmar pagamento, PDF também é persistido via `FileStorageService` (Plano 0) e `reciboArquivoId` gravado no pagamento. Inexistente → 404.

- [ ] **Step 1: Testes RED** — GET retorna 200 + content-type pdf + bytes começam com `%PDF`; após confirmar, `reciboArquivoId` não-nulo; tatuador de outro pagamento → 403.
- [ ] **Step 2–4:** falhar → dependência + service + endpoint → verdes + suite.

### Task 5: Visão do tatuador (próprio perfil financeiro)

**Files:**
- Modify: `TransacaoController` (novo endpoint "meus repasses") ou `PagamentoComissaoController`
- Test: `PagamentoComissaoControllerTest`

**Interfaces:**
- Produces: `GET /api/v1/financeiro/meus-repasses?inicio=&fim=` (TATUADOR) → `{trabalhos: Page (statusRepasse PENDENTE/AGENDADO/PAGO), pagamentos: [PagamentoComissaoDTO], adiantamentosEmAberto: BigDecimal}`. Escopo sempre do token — impossível ver de outro.

- [ ] **Step 1: Teste RED** — tatuador A autenticado vê só seus dados mesmo com seeds de B.
- [ ] **Step 2–4:** falhar → implementar → verdes + suite.

### Task 6 (front): telas

**Files (front):** Modify: `pages/Tatuadores.jsx` (GESTOR: clique abre detalhe com trabalhos + checkboxes + total → modal pagamento com dados bancários/upload comprovante), `pages/Comissao.jsx`/`CommissionCard.jsx` (visão tatuador: meus repasses), Create: `pages/Adiantamentos.jsx` (GESTOR), histórico de pagamentos com botão "Recibo PDF" (download).

- [ ] **Step 1: Testes jest** — seleção soma total; abatimento exibido; tatuador não vê ações de gestor.
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar.**
- [ ] **Step 4: `npm test`** + smoke manual do fluxo completo.
