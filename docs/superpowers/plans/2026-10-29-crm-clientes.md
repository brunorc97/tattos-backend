# Plano 3 — CRM de Clientes

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Aba Clientes (tatuador/gestor) com busca textual, visão por role, e perfil 360° do cliente: histórico de contato, trabalhos com foto, projetos não convertidos, aniversário, fichas e alergias.

**Spec:** `2026-10-29-tattos-spec-v2.md` §4. **Depende do Plano 0** (busca textual Task 4, `dataNascimento` Task 1, upload Task 3) e projeta dados do Plano 2 (projetos recusados).

**Architecture:** Perfil 360° = endpoint de agregação read-only (`ClientePerfilDTO`) compondo repositories existentes — sem tabela nova, exceto `ContatoCliente` (histórico CRM) e `TrabalhoRealizado` (foto+tatuador+data).

## Review Focus
1. **Tatuador acessando perfil de cliente que nunca agendou com ele** → 403/404. Teste Task 2.
2. **Busca por telefone com formatação diferente** ("(11) 99999-0000" vs "11999990000") → match por dígitos normalizados. Teste Task 1.
3. **Cliente inativo/deletado** não aparece em busca de CRM. Teste Task 1.
4. **Foto de trabalho não-imagem** → 400 (Plano 0). Teste Task 4.
5. **Perfil de cliente inexistente** → 404 limpo. Teste Task 3.

### Task 1: Escopo de clientes por role + busca normalizada

**Files:**
- Modify: `UsuarioController.java` (`GET /clientes` do Plano 0), `UsuarioRepository.java`
- Test: estender `ClienteBuscaTest.java`

**Interfaces:**
- Produces: `GET /api/v1/usuarios/clientes?query=` → TATUADOR vê apenas clientes com pelo menos um `Agendamento` com ele; GESTOR vê todos; apenas `ativo=true`; telefone comparado por dígitos (`REPLACE` de não-dígitos no @Query ou normalização no service).

- [ ] **Step 1: Testes RED** — tatuador A não vê cliente exclusivo do tatuador B; busca "11999990000" encontra telefone gravado como "(11) 99999-0000"; cliente inativo ausente.
- [ ] **Step 2–4:** falhar → implementar (query com `EXISTS` agendamento; normalização) → verdes + suite.

### Task 2: Histórico de contato (CRM)

**Files:**
- Create: `model/ContatoCliente.java`, repository, `V20__contato_cliente.sql`, endpoints em novo `ClienteCrmController.java`
- Test: `src/test/java/com/tattos/controller/ClienteCrmControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/clientes/{clienteId}/contatos` (GESTOR,TATUADOR com vínculo) body `{tipo (LIGACAO/WHATSAPP/EMAIL/PRESENCIAL), resumo, dataContato?}` → 201; `GET /api/v1/clientes/{clienteId}/contatos` → Page ordenado desc.

- [ ] **Step 1: Testes RED** — registrar contato (201); tatuador sem vínculo → 403; listar ordenado.
- [ ] **Step 2–4:** falhar → migration + CRUD → verdes + suite.

### Task 3: Perfil 360° agregado

**Files:**
- Create: `dto/response/ClientePerfilDTO.java`, método `perfil` em `ClienteCrmController`
- Test: `ClienteCrmControllerTest`

**Interfaces:**
- Consumes: AgendamentoRepository, FichaAnamneseRepository, ProjetoRepository (Plano 2), TransacaoFinanceiraRepository, ContatoCliente (Task 2).
- Produces: `GET /api/v1/clientes/{id}/perfil` (GESTOR; TATUADOR com vínculo) → `{cliente: UsuarioResponseDTO (com dataNascimento), totalTrabalhos: long, projetos: {total, aprovados, naoAprovados: [{id, nome, motivoRecusa, valorOrcamento?}]}, ultimasFichas: [{id, data, alertas}], alergias: String (da ficha mais recente), ultimosContatos: [...], proximoAniversario: LocalDate}`. Inexistente → 404.

- [ ] **Step 1: Testes RED** — agregado com seeds de cada fonte; tatuador sem vínculo → 403; 404.
- [ ] **Step 2–4:** falhar → service de agregação read-only → verdes + suite.

### Task 4: Trabalhos realizados com foto

**Files:**
- Create: `model/TrabalhoRealizado.java`, repository, `V21__trabalho_realizado.sql`, endpoints em `ClienteCrmController`
- Test: `ClienteCrmControllerTest`

**Interfaces:**
- Consumes: upload do Plano 0 (`arquivoId`, imagens PNG/JPEG).
- Produces: `POST /api/v1/clientes/{clienteId}/trabalhos` (GESTOR,TATUADOR) body `{descricao, tatuadorId, data, imagemId?, agendamentoId?}` → 201; `GET .../trabalhos` → lista. Entra no `perfil.totalTrabalhos`.

- [ ] **Step 1: Testes RED** — criar com imagem (201) e sem (201); tatuadorId inexistente → 404; listar no perfil incrementa total.
- [ ] **Step 2–4:** falhar → implementar → verdes + suite.

### Task 5 (front): Aba Clientes + perfil

**Files (front):** Create: `pages/Clientes.jsx` (lista + busca textual + filtros), `pages/ClientePerfil.jsx` (seções: dados+aniversário, contatos com formulário, trabalhos com upload de foto, projetos recusados com motivo, fichas/alergias); Modify: `App.jsx`, `Sidebar.jsx` (item "Clientes", roles GESTOR/TATUADOR).

- [ ] **Step 1: Testes jest** — item de menu por role; busca chama `query=`; perfil renderiza seções com mocks.
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar** telas consumindo Tasks 1–4.
- [ ] **Step 4: `npm test`** + smoke manual.
