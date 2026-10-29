# TattOS Spec v2 — Master Plan (índice de sub-planos)

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans, executando um sub-plano por vez, na ordem abaixo.

**Spec:** a mensagem do usuário de 2026-10-29 (10 seções: Navegação, Anamnese, Projetos/Orçamentos, CRM Clientes, Financeiro+Maquininhas, Agenda/Sessões, Bancada, Estoque, Comissões/Adiantamentos, Promoções/Cupons). Copiada verbatim para `docs/superpowers/plans/2026-10-29-tattos-spec-v2.md`.

**Estado mapeado (exploração 2026-10-29):** back = Spring Boot 4 / Java 25 / Flyway (última: V15, próxima V16); front = React 19 + Vite + Tailwind + recharts + axios, JWT em sessionStorage, `ProtectedRoute allowedRoles`, Sidebar com `navItems[].roles`.

## Decisões de escopo (derivadas do spec + exploração)

- **Bancada não vira entidade**: `Agendamento.cabine` (String livre) já cobre verificação de disponibilidade via query por cabine+dataHora. Uma tela de "Bancada" consome a agenda existente. Criar entidade agora seria YAGNI.
- **Tattoo AI**: não existe no código atual — o resquício é `SmartAiAssistant` no Dashboard; removê-lo atende "remover módulo".
- **Custo médio de estoque** exige histórico de entradas com preço → nova entidade `MovimentacaoEstoque` (também atende "alocar tatuador na saída").
- **Uploads**: backend não tem nada → building block único (`/api/v1/files`) consumido por comprovante de sinal, recibos e imagens de trabalhos.
- **Ordem**: fundação primeiro (bug da anamnese quebra uso atual; clientes/agenda são pré-requisitos de projetos e comissões).

## Sub-planos

| # | Plano | Cobre do spec | Por quê nessa posição |
|---|-------|---------------|----------------------|
| 0 | `2026-10-29-fundacao-fixes-e-building-blocks.md` | §1 (menus, roles, terminologia, Tattoo AI), §2 (bug criação de ficha + regras de visão), building blocks: upload de arquivos, `dataNascimento`+dados bancários em Usuario, busca de clientes por nome/email/telefone | Desbloqueia tudo; corrige o que está quebrado hoje |
| 1 | `2026-10-29-agenda-e-bancada.md` | §6 Agenda (busca, visões por role, solicitação do cliente com ideia/imagem/preferências, matching por estilo), §7 Bancada | Consome busca de clientes (0) |
| 2 | `2026-10-29-projetos-orcamentos-kanban.md` | §3 completo (vínculo cliente, prazo, aprovação/recusa, sinal, comprovante PDF, flag atraso, permissões) | Consome upload (0) e clientes (0) |
| 3 | `2026-10-29-crm-clientes.md` | §4 completo (aba Clientes, perfil 360°, histórico, trabalhos com foto, aniversário) | Consome 0; projeta dados de 2 |
| 4 | `2026-10-29-financeiro-e-maquininhas.md` | §5 (receita×despesa, taxas por operadora/modalidade, valor líquido, dashboard interativo) | Consome 0; independente dos demais |
| 5 | `2026-10-29-comissoes-adiantamentos-recibos.md` | §9 completo (seleção de trabalhos, pagamento agendado, adiantamentos com abatimento, recibo PDF, visão do tatuador) | Consome upload (0) e financeiro (4) |
| 6 | `2026-10-29-promocoes-cupons.md` | §10 completo (cupons por gestor/tatuador, abrangência, %/fixo, aplicação no agendamento) | Consome agenda (1) para aplicar desconto |

## Convenções globais (valem para todos os sub-planos)

- Back: TDD com o padrão existente (`@SpringBootTest` + `@ActiveProfiles("test")` + H2; MockMvc para controller), Flyway `V{n}__...sql` a partir de **V16**, endpoints novos sob `/api/v1/...` (aliases legados só se substituírem rota existente), mensagens em `messages.properties` (+`messages_en.properties`), `@PreAuthorize` em todo endpoint novo, `@Schema(example=...)` em DTOs novos.
- Front: páginas novas roteadas em `App.jsx` com `ProtectedRoute allowedRoles`, item em `Sidebar navItems` com `roles`, chamadas via `src/services/api.ts` (já trata 401/403/409/422 e envia `X-Correlation-ID`), listagens consomem `Page` (`response.data.content`), erros de formulário via `getFieldErrors`.
- **Sem commits pelo executor** — o usuário commita ao final (instrução explícita desta sessão).
- Verificação: back `./gradlew test`; front `npm test` / suite existente.
