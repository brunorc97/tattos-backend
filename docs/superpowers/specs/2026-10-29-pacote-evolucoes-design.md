# Design — Pacote de evoluções TattOS (Studio, Estoque, Agenda, Orçamentos, Repasse, Bancada)

Data: 2026-10-29
Autor: Bruno + Copilot

## Intenção

Seis frentes de trabalho levantadas pelo Bruno, unindo o prompt da API do Studio (vitrine/trabalhos/ideias/avaliações) e pontos levantados em conversa: repasse pago, alocação de bancada, revisão da agenda, fluxo de entrada/saída de estoque com custo médio, e fila de orçamentos para o tatuador.

## Estado atual (levantamento)

- **Studio**: nada existe. `Usuario` não tem `fotoUrl`; `Tatuador` herda `especialidade` e `bio` de `Usuario`. `TrabalhoRealizado` liga cliente+tatuador+agendamento+imagem (uso financeiro pós-sessão).
- **Estoque**: `Insumo` com entrada/saída por PATCH que só ajusta saldo. Sem histórico, sem custo na entrada, sem registro de quem retirou, sem custo médio. `valorUnitario` é estático no cadastro.
- **Agenda**: fluxo básico existe (criação com validação de conflito tatuador/cabine, status AGENDADO→CONFIRMADO→REALIZADO/CANCELADO, sinal PIX). Lacunas: sem transição automática ao pagar sinal, sem validação de horário passado/comercial, `cabine` é String livre, sem visão de agenda consolidada.
- **Orçamento**: `Orcamento` só tem nome/valor/data/projeto. Sem status, sem cliente, sem tatuador, sem fila.
- **Repasse**: `PagamentoComissao` já tem status AGENDADO/PAGO e endpoint de confirmar. `StatusRepasse` (PENDENTE/AGENDADO/PAGO) por transação **não é exposto no DTO**. Recibo PDF não inclui período. `FechamentoComissaoDTO` existe mas não é usado em endpoint.
- **Bancada**: não existe entidade; só grade calculada de disponibilidade 09h–19h por `cabine` (String).

## Decisões já tomadas pelo Bruno

- GESTOR posta trabalho de qualquer tatuador; TATUADOR só os dele.
- CLIENTE só vê a vitrine `/studio`; GESTOR/TATUADOR têm aba separada para alimentá-la.
- Saída de estoque exige indicar qual tatuador (ou gestor) retirou.
- Entrada de estoque registra custo do material e recalcula custo médio com os custos anteriores.
- Telas de entrada/saída de estoque em popup (front).
- Recibo PDF com período.
- Tatuador tem fila de orçamentos organizada.

## Abordagens consideradas

### Studio — trabalhos da vitrine
- A) Entidade `trabalho` nova para a vitrine ✅ **(escolhida pelo Bruno)**: permite foto solta de portfólio sem cliente associado e separa vitrine do financeiro. Vínculo com `TrabalhoRealizado`/cliente é opcional (só quando o trabalho vem de sessão real, para habilitar avaliação).
- B) Reutilizar `TrabalhoRealizado` como vitrine: acopla portfólio ao financeiro e obriga cliente em todo trabalho — rejeitada (Bruno quer portfólio aberto).

### Estoque — histórico e custo médio
- **A) Entidade `movimentacao_estoque`** (entrada/saída, quantidade, custo unitário na entrada, quem retirou, motivo, data) + custo médio ponderado recalculado na entrada (recomendada).
- B) Manter só campos no Insumo: perde histórico e auditoria — rejeitada.

### Bancada — alocação por horário
- **A) Entidade `bancada` + agendamento passa a referenciar `bancada_id`**; alocação acontece pelo próprio agendamento (recomendada) — evita estrutura paralela de "reserva".
- B) Entidade separada de alocação independente do agendamento: mais flexível (bloqueios sem cliente), mas duplica lógica de conflito. Fica como evolução futura.

### Agenda
- Revisão incremental sobre o fluxo existente (não reescrever): automações e validações pontuais.

## Escopo por frente

### 1. Studio API (`/api/v1/studio`)
Conforme o prompt original, sem alterar contratos consumidos por `studioService.js`:
- Entidades: `studio_info` (singleton), `trabalho` (tatuador, título, estilo, descrição, fotoUrl, data, opcional `trabalho_realizado_id` + `cliente_id` para avaliação), `ideia_tatuagem`, `avaliacao` (trabalho, cliente, notaTatuagem 1–5, notaTatuador 1–5, comentário, data; única por trabalho).
- `Usuario.fotoUrl` (migration).
- Endpoints: GET/PATCH studio; GET tatuadores (com mediaAvaliacao = média de (notaTatuagem+notaTatuador)/2 e totalAvaliacoes); GET/POST trabalhos (POST: GESTOR qualquer tatuador, TATUADOR apenas `tatuadorId` = próprio id, senão 403); GET/POST ideias; avaliações: GET geral, GET pendentes (CLIENTE via JWT), GET minhas, POST (CLIENTE dono do trabalho; 409 se duplicada, 403 se de outro cliente).
- "Trabalho concluído avaliável": derivado de `TrabalhoRealizado` do cliente, ainda sem avaliação.
- Regra fina via `@PreAuthorize` + validação no service; requestMatchers no SecurityConfig.
- Migrations V28+: `studio_info`, `trabalho`, `ideia_tatuagem`, `avaliacao`, `foto_url` em `usuarios`.
- OpenAPI + testes de integração (401 sem token, 403 perfil errado, 409 duplicada, 403 outro cliente, fluxo feliz).

### 2. Estoque — movimentações, custo médio e responsável
- Migration: `movimentacao_estoque` (id, insumo_id, tipo ENTRADA/SAIDA, quantidade, custo_unitario (entrada), retirado_por_id (saída, obrigatório), motivo, created_at/created_by).
- Entrada: registra custo e recalcula **média simples** dos custos de entrada: `novoMedio = média de todos os custo_unitario das movimentações de ENTRADA do insumo` (decisão do Bruno); atualiza `valorUnitario` do insumo com a média.
- Saída: exige `retiradoPorId` (tatuador ou gestor). **GESTOR pode registrar saída em nome de um tatuador** — `retiradoPorId` = tatuador; `createdBy` (auditoria) = gestor logado.
- Endpoints: POST `/api/v1/estoque/{id}/entrada` (quantidade + custoUnitario + motivo), POST `/{id}/saida` (quantidade + retiradoPorId + motivo), GET `/{id}/movimentacoes` (histórico). Manter PATCHs antigos como deprecated (OpenAPI) por uma versão.
- Front (fora deste repo): popup único de movimentação.

### 3. Agenda — revisão
Causa raiz encontrada (front `Agenda.jsx`): a aba sempre envia `data=<hoje>` (selectedDate default = hoje), então só mostra sessões do dia; o dashboard lista as próximas sem filtro de data. **Correção principal é no front**: carregar sem `data` usando `futuras=true` (parâmetro já existente no backend) ou evidenciar o filtro de dia.
Backend, complementos:
- Confirmar sinal pago ⇒ status vira CONFIRMADO automaticamente (em `marcarSinalPago`).
- Validar na criação: não aceitar data/hora no passado; respeitar grade 09h–19h (alinhar com a grade da bancada).
- Endpoint de visão de agenda consolidada por período (`GET /api/v1/agendamentos/agenda?inicio&fim`) com escopo por role.

### 4. Orçamentos — fila do tatuador
Fluxo real (confirmado pelo Bruno): **cliente envia o Projeto (ideia) → tatuador desenha e responde com o Orçamento** (ex.: "dragão 20cm nas costas: R$1300 em 2 sessões" ou "R$500/sessão, 2–3 sessões"). Orçamento e Projeto andam juntos.
- A fila do tatuador deriva do **Projeto** (que já tem cliente/tatuador/status); enriquecer `Orcamento`:
  - Migration: adicionar a `orcamento`: `valor_total`, `valor_sessao`, `sessoes_min`, `sessoes_max`, `tipo_cobranca` (FECHADO/POR_SESSAO), `observacoes`, `status` (RASCUNHO/ENVIADO/ACEITO/RECUSADO).
- `GET /api/v1/projetos/fila` (TATUADOR: os seus, ordenados por status + data; GESTOR: todos ou por tatuador via param) — "fila bonitinha" = projetos aguardando orçamento/resposta primeiro.
- POST/PUT de orçamento vinculado ao projeto; ao ACEITAR orçamento, projeto avança no fluxo existente de aprovação/sinal.

### 5. Repasse/fechamento — visibilidade de pago + período no recibo
- Expor `statusRepasse` em `TransacaoResponseDTO` e no DTO de trabalhos por tatuador (`/comissoes/{tatuadorId}/trabalhos` e `/meus-repasses`).
- Fechamento: usar `FechamentoComissaoDTO` em endpoint JSON (`GET /api/v1/financeiro/comissoes/{tatuadorId}/fechamento?inicio&fim`) mostrando por item o status (PENDENTE/AGENDADO/PAGO) — resolve "hoje não vê se já foi pago".
- Recibo PDF: adicionar período (início–fim) do fechamento; guardar `periodoInicio/periodoFim` no `PagamentoComissao` (migration) para o PDF do histórico.

### 6. Bancada — cadastro e alocação por horário
- Migration: `bancada` (id, nome/numero, ativa).
- CRUD básico (GESTOR): GET/POST/PUT/DELETE `/api/v1/bancadas`.
- Agendamento passa a referenciar `bancada_id` (migration com backfill a partir de `cabine` String; manter coluna antiga por uma versão).
- Alocação = criar agendamento com bancada + horário (TATUADOR e GESTOR); validação de conflito passa a usar `bancada_id`. Grade de disponibilidade passa a usar id.

## Ordem sugerida de execução

1. Repasse/fechamento (pequeno, destrava visibilidade financeira)
2. Estoque (médio, independe de tudo)
3. Agenda (validações + automação do sinal)
4. Bancada (depende da agenda)
5. Orçamentos — fila
6. Studio API (maior; em fatias: info+tatuadores → trabalhos+ideias → avaliações)

## Não-objetivos

- Front-end (outro repo) — aqui só os contratos.
- Notificações/e-mail/WhatsApp.
- Bloqueio de bancada sem agendamento (evolução futura).

## Perguntas em aberto para o Bruno

1. ~~Orçamento vs. Projeto~~ — **respondida**: andam juntos; cliente envia projeto, tatuador responde com orçamento (valor fechado ou por sessão). Fila deriva do Projeto; Orcamento ganha campos de sessões/tipo de cobrança.
2. ~~"Agenda não funciona hoje"~~ — **respondida**: aba Agenda filtra sempre pelo dia atual (front); backend OK. Correção no front (sem `data`, usando `futuras=true`) + melhorias de backend (auto-confirmar no sinal pago, validar passado/grade, endpoint de agenda por período).
3. ~~Vitrine com fotos soltas~~ — **respondida**: sim, portfólio aberto; entidade `trabalho` separada confirmada.
4. ~~Custo médio~~ — **respondida**: média **simples** dos custos de entrada.
5. ~~Saída em nome de tatuador~~ — **respondida**: sim, gestor registra indicando o tatuador; auditoria guarda quem registrou.

## O que fica a critério do builder

- Ordenação da fila de orçamentos (proposta: status depois dataCriacao ASC).
- Manter PATCHs antigos de estoque como deprecated vs remover.
- Nomes exatos de migrations (seguir padrão `V{n}__snake_case.sql`, começando em V28).
