# Plano 0 — Fundação: Fixes, Navegação, Building Blocks

> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:subagent-driven-development ou superpowers:executing-plans. Steps com `- [ ]`.

**Goal:** Corrigir o que está quebrado (anamnese sem endpoint de criação, acessos redirecionando), alinhar navegação/terminologia por role, e criar os building blocks (upload de arquivos, novos campos de Usuario, busca de clientes) que os sub-planos 1–6 consomem.

**Architecture:** Back Spring Boot 4 (`com.tattos`), front React 19+Vite (`../tattos-frontend`). Convenções globais no master plan (`2026-10-29-tattos-spec-v2-master.md`).

**Spec:** `2026-10-29-tattos-spec-v2.md` §1 e §2.

## Global Constraints
- Próxima migration Flyway: **V16**.
- Back: endpoints novos sob `/api/v1/...`; mensagens via `messages.properties`; TDD (`./gradlew test`).
- Front: mudanças em `../tattos-frontend` — rodar `npm test -- --watchAll=false` no diretório do front.
- **Sem commits pelo executor.**

## Review Focus
1. **Ficha criada por gestor/tatuador para cliente inexistente** → 404 limpo, não 500. Teste no Task 2.
2. **Cliente tentando ver ficha de outro** → 403/404, nunca dados. Teste no Task 2.
3. **Upload de 20MB ou arquivo .exe** → 400/413 com mensagem amigável, sem travar request. Teste no Task 3.
4. **Busca de clientes sem critério** (query vazia) → lista paginada padrão, não erro nem dump completo sem paginação. Teste no Task 5.
5. **Menu do tatuador** não pode vazar rota "Tatuadores" nem Dashboard geral — teste de renderização por role no Task 6.

---

### Task 1: Migration V16 — novos campos de Usuario

**Files:**
- Create: `src/main/resources/db/migration/V16__usuario_nascimento_e_dados_bancarios.sql`

**Interfaces:**
- Produces: colunas `usuarios.data_nascimento DATE NULL`, `usuarios.pix_chave VARCHAR(140) NULL`, `usuarios.banco VARCHAR(60) NULL`, `usuarios.agencia VARCHAR(20) NULL`, `usuarios.conta VARCHAR(30) NULL`.

- [ ] **Step 1: Escrever a migration V16** com os 5 `ADD COLUMN` acima (todos NULL, sem default).
- [ ] **Step 2: Atualizar entidade** `model/Usuario.java` com os 5 campos (`LocalDate dataNascimento`, `String pixChave/banco/agencia/conta`) e expor em `UsuarioResponseDTO` (sem `senha`, obviamente).
- [ ] **Step 3: Teste** — em `AuthServiceTest` ou novo `UsuarioMappingTest`: registrar cliente com `dataNascimento` via `RegisterRequestDTO` (adicionar campo opcional ao DTO) e assertar retorno no `UsuarioResponseDTO`.
- [ ] **Step 4: `./gradlew test`** verde. Sem commit (usuário commita).

### Task 2: Endpoint de criação de Ficha de Anamnese (bug crítico §2)

**Files:**
- Modify: `src/main/java/com/tattos/controller/FichaAnamneseController.java`
- Modify: `src/main/java/com/tattos/service/FichaAnamneseService.java` (método `criar`/`registrarFicha` existe sem uso — linhas 56-61; revisar assinatura)
- Test: `src/test/java/com/tattos/controller/FichaAnamneseControllerTest.java` (novo)

**Interfaces:**
- Produces: `POST /api/v1/fichas-anamnese` body `FichaAnamneseRequestDTO` → 201 `FichaAnamneseResponseDTO`. Regras: CLIENTE só cria para si (`clienteId` do token); GESTOR/TATUADOR para qualquer `clienteId` válido; cliente inexistente → 404.

- [ ] **Step 1: Testes RED** — criar ficha como CLIENTE (201, força próprio id), como GESTOR para cliente seed (201), para cliente inexistente (404), sem auth (401).
- [ ] **Step 2: Rodar e ver 404/405** (endpoint não existe).
- [ ] **Step 3: Implementar** `POST /` no controller com `@PreAuthorize("hasAnyRole('GESTOR','TATUADOR','CLIENTE')")` + lógica de role no service.
- [ ] **Step 4: Testes verdes + suite completa** (`./gradlew test`).
- [ ] **Step 5 (front):** verificar que `pages/Anamnese.jsx` (ou equivalente) passa a chamar o novo POST e exibe erros via `getFieldErrors`.

### Task 3: Building block — upload de arquivos

**Files:**
- Create: `src/main/java/com/tattos/controller/FileController.java`, `src/main/java/com/tattos/service/FileStorageService.java`
- Create: `src/main/resources/db/migration/V17__arquivos.sql`
- Test: `src/test/java/com/tattos/controller/FileControllerTest.java`

**Interfaces:**
- Produces: `POST /api/v1/files` (multipart, campo `file`) → 201 `{id, nomeOriginal, contentType, tamanhoBytes, url}`; `GET /api/v1/files/{id}` → bytes com Content-Type. Limite 10 MB; tipos permitidos: PDF, PNG, JPEG. Tabela `arquivos(id, nome_original, content_type, tamanho_bytes, caminho, created_by, created_at)`. Storage em disco `uploads/` (dir configurável via `app.storage.upload-dir`, default `./uploads`, gitignored). Demais planos referenciam arquivos por `arquivoId`.

- [ ] **Step 1: Teste RED** — upload de PDF fake via `MockMvc.multipart` → 201 + jsonPath `$.url`; upload de `.exe` → 400; arquivo >10MB → 400/413.
- [ ] **Step 2: Rodar e falhar.**
- [ ] **Step 3: Implementar** controller+service+entity `Arquivo`+repository+migration V17; `spring.servlet.multipart.max-file-size: 10MB` no application.yml.
- [ ] **Step 4: Testes verdes + suite.**

### Task 4: Busca de clientes por nome/email/telefone (§4/§6)

**Files:**
- Modify: `src/main/java/com/tattos/controller/UsuarioController.java`, `repository/UsuarioRepository.java`, `service/` (criar `UsuarioService` se inexistente — controller chama `authService`/repo direto hoje)
- Test: `src/test/java/com/tattos/controller/ClienteBuscaTest.java`

**Interfaces:**
- Produces: `GET /api/v1/usuarios/clientes?query={texto}&page=&size=` → `Page<UsuarioResponseDTO>`; query vazia → todos paginados; match case-insensitive em nome/email/telefone (`LIKE %q%`). Roles: GESTOR, TATUADOR. (Filtro "tatuador vê só seus clientes" é do sub-plano CRM — aqui só a busca textual.)

- [ ] **Step 1: Teste RED** — seed 3 clientes; `?query=maria` retorna 1; `?query=1199` (telefone) retorna por telefone; sem query retorna página com todos.
- [ ] **Step 2–4:** falhar → implementar (`findClientesByQuery` com `@Query`) → verde + suite.

### Task 5: Regras de visualização de fichas (§2)

**Files:**
- Modify: `FichaAnamneseController.java` (`GET /`, `GET /cliente/{id}`) e service
- Test: estender `FichaAnamneseControllerTest.java`

**Interfaces:**
- Produces: `GET /api/v1/fichas-anamnese?tatuadorId=&clienteId=&mes=&ano=` → GESTOR: todas com filtros; TATUADOR: só fichas de clientes com agendamento com ele (ignora `tatuadorId` alheio); CLIENTE: só as próprias (qualquer outro `clienteId` → 403).

- [ ] **Step 1: Testes RED** — tatuador A não vê ficha de cliente do tatuador B; gestor filtra por `tatuadorId`+`mes/ano`; cliente pedindo `clienteId` de outro → 403.
- [ ] **Step 2–4:** falhar → implementar filtros no repository/service com resolução do usuário logado → verde + suite.

### Task 6 (front): Navegação, roles e terminologia (§1)

**Files (front `../tattos-frontend`):**
- Modify: `src/components/layout/Sidebar.jsx`, `src/App.jsx`, `src/components/layout/Header.jsx`, `src/pages/Dashboard.jsx`, `src/pages/Tatuadores.jsx`, demais arquivos com "Ateliê/Atelier/Aerografia"
- Delete/Remove usage: `src/components/ui/SmartAiAssistant.jsx` (remover do Dashboard)
- Test: `src/components/layout/__tests__/Sidebar.test.jsx` (ou suíte existente equivalente)

**Interfaces:**
- Consumes: role via `AuthContext` (`user.role`).

- [ ] **Step 1: Teste RED (jest+testing-library)** — renderizar Sidebar com `role: 'ROLE_TATUADOR'` → não contém item "Tatuadores" nem "Dashboard"; com `ROLE_GESTOR` → contém. Rota `/tatuadores` com `allowedRoles` sem TATUADOR → acesso negado.
- [ ] **Step 2: Falhar.**
- [ ] **Step 3: Implementar:**
  - Sidebar: `roles: ['ROLE_GESTOR']` no item Tatuadores; esconder Dashboard para TATUADOR (rota `/` redireciona tatuador para `/perfil` ou `/agenda`).
  - App.jsx: `ProtectedRoute` de `/tatuadores` e `/financeiro` restritos a GESTOR; aplicar layout por role (usar `LayoutTatuador`/`LayoutCliente` que já existem) ou ajustar `LayoutGestor` para centralizar menu e logo à esquerda conforme spec.
  - Renomear textos: "Ateliê/Atelier" → "Studio" em Header/Sidebar/AccessDenied/Tatuadores/ChromeButton/LowriderCard; Dashboard: remover `SmartAiAssistant` e o título/mock "Aerografia" (gráfico vira "Evolução Financeira" com dados reais ou placeholder neutro).
  - Investigar e corrigir o redirect para home ao clicar em "Tatuadores" com usuário de teste (causa provável: rota permitindo TATUADOR → página chama endpoint que retorna 403 → `tattos:access-denied` → redirect; com a rota restrita a GESTOR o problema some — confirmar e registrar no teste).
- [ ] **Step 4: `npm test -- --watchAll=false`** verde; smoke manual `npm run dev` navegando com as 3 roles.

### Task 7 (front): Tela de criação de ficha plugada

**Files (front):** Modify: página de anamnese existente (`pages/Anamnese.jsx` ou onde o formulário vive).

- [ ] **Step 1:** Formulário passa a POSTar em `/api/v1/fichas-anamnese` (Task 2), exibe `fieldErrors` por campo, e redireciona para a ficha criada. Listagem usa os filtros da Task 5 conforme role.
- [ ] **Step 2:** Teste de integração do front (jest mock de api) + smoke manual das 3 roles criando ficha.

---

## Self-review aplicado
- Spec §1 → Tasks 6; §2 → Tasks 2/5/7; building blocks consumidos pelos planos 1–6 → Tasks 1/3/4. ✅
- Ordem de execução: 1 → 2 → 3 → 4 → 5 → 6 → 7.
