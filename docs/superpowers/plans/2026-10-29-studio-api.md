# Studio API — Vitrine, Trabalhos, Ideias e Avaliações — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** API real para a vitrine pública do estúdio (`/studio` no front) e avaliações do cliente, substituindo o mock de `studioService.js` — contratos exatos abaixo, **não alterar payloads**.

**Architecture:** Novo módulo `studio`: entidades `StudioInfo` (singleton), `Trabalho` (portfólio; vínculo opcional com `TrabalhoRealizado`/cliente para habilitar avaliação), `IdeiaTatuagem`, `Avaliacao` (única por trabalho). `Usuario` ganha `fotoUrl`. Camadas controller → service → repository, DTOs separados, Bean Validation, autorização por role no SecurityConfig + `@PreAuthorize`.

**Tech Stack:** Spring Boot 4, Spring Security 7, Spring Data JPA, Flyway, Bean Validation, Springdoc OpenAPI, JUnit 5 + MockMvc + H2.

**Spec:** `docs/superpowers/specs/2026-10-29-pacote-evolucoes-design.md` (frente 1) — os contratos JSON abaixo são cópia do prompt do front e prevalecem.

## Contratos (fixos — base `/api/v1/studio`)

- `GET /studio` (autenticado, qualquer perfil) → `{ nome, descricao, endereco, horario, instagram }`
- `PATCH /studio` (GESTOR) — mesmos campos, todos opcionais → objeto atualizado
- `GET /studio/tatuadores` (todos) → `[{ id, nome, especialidade, bio, fotoUrl|null, mediaAvaliacao|null, totalAvaliacoes }]` — mediaAvaliacao = média de `(notaTatuagem + notaTatuador)/2`; null sem avaliações
- `GET /studio/trabalhos` (todos) → `[{ id, tatuadorId, tatuadorNome, titulo, estilo, descricao, fotoUrl|null, data: AAAA-MM-DD }]` (recentes primeiro)
- `POST /studio/trabalhos` (GESTOR/TATUADOR) — body `{ tatuadorId, titulo (obrigatório), estilo, descricao, fotoUrl }` → 201 mesmo formato do GET, `data` = hoje. TATUADOR só com `tatuadorId` = próprio (senão 403); GESTOR qualquer um.
- `GET /studio/ideias` (todos) → `[{ id, titulo, estilo, descricao, imagemUrl|null }]`
- `POST /studio/ideias` (GESTOR/TATUADOR) — `titulo` obrigatório, demais opcionais → 201.
- `GET /studio/avaliacoes` (todos) → `[{ id, trabalhoId, tatuadorId, tatuadorNome, clienteNome, notaTatuagem, notaTatuador, comentario, data }]` (recentes primeiro)
- `GET /studio/avaliacoes/pendentes` (CLIENTE, via JWT) → `[{ id, tatuadorId, tatuadorNome, titulo, estilo, dataConclusao }]` — trabalhos concluídos dele ainda não avaliados
- `GET /studio/avaliacoes/minhas` (CLIENTE) → avaliações que ele enviou (formato do GET geral)
- `POST /studio/avaliacoes` (CLIENTE) — body `{ trabalhoId, notaTatuagem, notaTatuador, comentario? }` → 201; 403 se trabalho de outro cliente; 409 se já avaliado.

## Global Constraints

- Migrations: **V33+** (após os planos anteriores; renumere para a próxima livre se necessário): `studio_info`, `trabalho`, `ideia_tatuagem`, `avaliacao`, `usuarios.foto_url`.
- Notas: inteiras 1–5, ambas obrigatórias (`@Min(1) @Max(5) @NotNull`).
- Uma avaliação por trabalho: constraint única `avaliacao.trabalho_id` + verificação no service → 409.
- "Trabalho avaliável" = `trabalho` com `cliente_id` preenchido (vindo de sessão real); trabalho de portfólio solto (sem cliente) **não** é avaliável e não aparece em pendentes.
- CLIENTE não cria/edita nada em `/studio` fora das avaliações; TATUADOR não edita info do estúdio.
- Testes de integração obrigatórios: 401 sem token, 403 perfil errado, 409 avaliação duplicada, 403 trabalho de outro cliente, fluxo feliz de cada endpoint.
- Documentar todos os endpoints com anotações OpenAPI (`@Tag`, `@Operation`, `@SecurityRequirement`) como nos controllers existentes.
- Testes: padrão repo H2 (spec citava Testcontainers — convenção do repo prevalece). Commits conventional em português.

## Review Focus

1. `mediaAvaliacao` com avaliações mistas (ex.: notas 5/4 e 3/2) → média de (5+4)/2 e (3+2)/2 = 3,75; arredondar 1 casa decimal (HALF_UP) — teste Task 4.
2. Trabalho de portfólio (sem cliente) → não pode ser avaliado (400/422) nem aparecer em pendentes — teste Task 6.
3. CLIENTE avaliando trabalho de outro → **403** (não 404 — não vazar existência é secundário aqui; o front espera 403) — teste Task 6.
4. PATCH /studio com body vazio `{}` → 200 sem alterar nada — teste Task 2.
5. Tatuador inativo/removido não deve aparecer na vitrine — filtrar `ativo = true` — teste Task 4.

---

### Task 1: Migrations + entidades + repositórios

**Files:**
- Create: `src/main/resources/db/migration/V33__studio.sql`
- Create: `src/main/java/com/tattos/model/StudioInfo.java`, `Trabalho.java`, `IdeiaTatuagem.java`, `Avaliacao.java`
- Create: `src/main/java/com/tattos/repository/{StudioInfoRepository, TrabalhoRepository, IdeiaTatuagemRepository, AvaliacaoRepository}.java`
- Modify: `src/main/java/com/tattos/model/Usuario.java` (add `fotoUrl`)

**Interfaces:**
- Produces:
  - `StudioInfo { id, nome, descricao, endereco, horario, instagram }`
  - `Trabalho { id, tatuador (ManyToOne Usuario, not null), cliente (ManyToOne Usuario, null), trabalhoRealizado (ManyToOne, null), titulo (not null), estilo, descricao, fotoUrl, data (LocalDate, not null) }`
  - `IdeiaTatuagem { id, titulo (not null), estilo, descricao, imagemUrl }`
  - `Avaliacao { id, trabalho (ManyToOne, not null, unique), cliente (not null), tatuador (not null), notaTatuagem (int), notaTatuador (int), comentario, data }`
  - `AvaliacaoRepository.existsByTrabalhoId(Long)`, `findByClienteIdOrderByDataDesc`, e query de média por tatuador: `SELECT AVG((a.notaTatuagem + a.notaTatuador)/2.0), COUNT(a) FROM Avaliacao a WHERE a.tatuador.id = :id`.
  - `TrabalhoRepository.findByClienteIdAndAvaliacaoIsNull` (pendentes — via `NOT EXISTS` subquery em Avaliacao), `findAllByOrderByDataDesc()`.

- [ ] **Step 1: Migration V33** com as 4 tabelas + `ALTER TABLE usuarios ADD COLUMN foto_url VARCHAR(500) NULL`. Constraint: `avaliacao.trabalho_id UNIQUE`. (Confirmar nome da tabela de usuários.)

- [ ] **Step 2: Entidades + repositórios** (padrão Lombok/auditing do repo). Seed opcional: 1 linha em `studio_info` com dados placeholder do estúdio.

- [ ] **Step 3: Verificar contexto** — `./gradlew test --tests "com.tattos.TattosApplicationTests"` → PASS.

- [ ] **Step 4: Commit**

```bash
git commit -am "feat(studio): entidades, repositorios e migrations da vitrine"
```

---

### Task 2: Studio info — GET/PATCH

**Files:**
- Create: `src/main/java/com/tattos/controller/StudioController.java` (`@RequestMapping("/api/v1/studio")`)
- Create: `src/main/java/com/tattos/service/StudioService.java`
- Create: `src/main/java/com/tattos/dto/{request/StudioInfoRequestDTO, response/StudioInfoResponseDTO}.java`
- Test: `src/test/java/com/tattos/controller/StudioInfoControllerTest.java`

**Interfaces:**
- Produces: `GET /api/v1/studio` (qualquer autenticado); `PATCH /api/v1/studio` (`@PreAuthorize("hasRole('GESTOR')")`, campos todos opcionais — atualizar só os non-null). Singleton: service usa `findFirstByOrderByIdAsc()` criando default vazio se ausente.

- [ ] **Step 1: Write the failing tests** — GET 200 autenticado; GET 401 sem token; PATCH como TATUADOR/CLIENTE → 403; PATCH `{}` → 200 sem mudança; PATCH com campos → reflete no GET.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar.**

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit** — `git commit -am "feat(studio): GET/PATCH de informacoes do estudio"`

---

### Task 3: Trabalhos — GET/POST

**Files:**
- Modify: `StudioController.java` (ou `StudioTrabalhoController` separado — preferir separado: `StudioTrabalhoController`, mesma base)
- Create: `src/main/java/com/tattos/service/TrabalhoService.java`
- Create: DTOs `TrabalhoRequestDTO { @NotNull Long tatuadorId; @NotBlank String titulo; String estilo; String descricao; @URL? String fotoUrl }`, `TrabalhoResponseDTO { id, tatuadorId, tatuadorNome, titulo, estilo, descricao, fotoUrl, data }` (`fromEntity`)
- Test: `src/test/java/com/tattos/controller/StudioTrabalhoControllerTest.java`

**Interfaces:**
- Produces: GET lista recentes primeiro; POST 201 com `data = LocalDate.now()`; regra de role: TATUADOR com `tatuadorId != logado.id` → 403 (lançar `AccessDeniedException`); `tatuadorId` inexistente/não-tatuador → 400.

- [ ] **Step 1: Failing tests** — fluxo feliz GESTOR postando para qualquer tatuador (201 + tatuadorNome); TATUADOR postando de si (201); TATUADOR postando de outro (403); sem token (401); `titulo` em branco (400 com fieldErrors); GET ordenado desc.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar** service + endpoints + DTOs.

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit** — `git commit -am "feat(studio): trabalhos do portfolio com regra de role por tatuador"`

---

### Task 4: Tatuadores da vitrine (com média de avaliações)

**Files:**
- Modify: `StudioController.java` ou novo `StudioTatuadorController`
- Create: `src/main/java/com/tattos/dto/response/TatuadorVitrineResponseDTO.java`
- Modify: `StudioService.java` ou novo `TatuadorVitrineService`
- Test: `src/test/java/com/tattos/controller/StudioTatuadoresTest.java`

**Interfaces:**
- Consumes: `UsuarioRepository` (buscar ROLE_TATUADOR ativos — ver como `GET /api/v1/usuarios/tatuadores` faz), `AvaliacaoRepository` query de média (Task 1), `Usuario.fotoUrl` (Task 1).
- Produces: `GET /api/v1/studio/tatuadores` → lista com `mediaAvaliacao` (BigDecimal 1 casa, null sem avaliações) e `totalAvaliacoes`.

- [ ] **Step 1: Failing tests** — tatuador sem avaliação → `mediaAvaliacao: null, totalAvaliacoes: 0`; com avaliações mistas → média 1 casa decimal; inativo não aparece.

- [ ] **Step 2: Run to fail.**

- [ ] **Step 3: Implementar.**

- [ ] **Step 4: Run to pass** → PASS.

- [ ] **Step 5: Commit** — `git commit -am "feat(studio): vitrine de tatuadores com media de avaliacoes"`

---

### Task 5: Ideias — GET/POST

**Files:**
- Create: `StudioIdeiaController`, `IdeiaTatuagemService`, DTOs `IdeiaRequestDTO { @NotBlank titulo; estilo; descricao; imagemUrl }`, `IdeiaResponseDTO`
- Test: `src/test/java/com/tattos/controller/StudioIdeiaControllerTest.java`

**Interfaces:**
- Produces: `GET /api/v1/studio/ideias` (todos), `POST /api/v1/studio/ideias` (GESTOR/TATUADOR) → 201.

- [ ] **Step 1: Failing tests** — fluxo feliz 201; `titulo` branco → 400; CLIENTE post → 403; sem token → 401.
- [ ] **Step 2: Run to fail.**
- [ ] **Step 3: Implementar.**
- [ ] **Step 4: Run to pass** → PASS.
- [ ] **Step 5: Commit** — `git commit -am "feat(studio): ideias de tatuagem GET/POST"`

---

### Task 6: Avaliações — GET geral/pendentes/minhas + POST

**Files:**
- Create: `StudioAvaliacaoController`, `AvaliacaoService`, DTOs `AvaliacaoRequestDTO { @NotNull Long trabalhoId; @NotNull @Min(1) @Max(5) Integer notaTatuagem; idem notaTatuador; String comentario; }`, `AvaliacaoResponseDTO`, `TrabalhoPendenteResponseDTO { id, tatuadorId, tatuadorNome, titulo, estilo, dataConclusao }`
- Test: `src/test/java/com/tattos/controller/StudioAvaliacaoControllerTest.java`

**Interfaces:**
- Consumes: tudo das tasks 1 e 3; `TrabalhoRealizadoRepository` (para derivar dataConclusao quando houver vínculo; para trabalho com `cliente` direto usar `trabalho.data`).
- Produces:
  - `GET /avaliacoes` (todos, recentes primeiro)
  - `GET /avaliacoes/pendentes` (`hasRole('CLIENTE')`) — trabalhos do logado com `cliente_id = logado.id` e sem avaliação
  - `GET /avaliacoes/minhas` (`hasRole('CLIENTE')`)
  - `POST /avaliacoes` (`hasRole('CLIENTE')`) — regras: trabalho existe e tem cliente (senão 422 "trabalho não avaliável"); `trabalho.cliente.id == logado.id` senão **403**; `!existsByTrabalhoId` senão **409** (`ConflictException`); `data = hoje`; `tatuador` copiado do trabalho.

- [ ] **Step 1: Failing tests** — os 5 cenários obrigatórios do spec (401, 403 perfil, 409 duplicada, 403 outro cliente, feliz) + pendentes lista só os não avaliados + minhas retorna formato geral + avaliar trabalho de portfólio (sem cliente) → 422.
- [ ] **Step 2: Run to fail.**
- [ ] **Step 3: Implementar.**
- [ ] **Step 4: Run to pass** → PASS.
- [ ] **Step 5: Commit** — `git commit -am "feat(studio): avaliacoes de trabalhos com validacao de propriedade"`

---

### Task 7: SecurityConfig + OpenAPI + verificação final

**Files:**
- Modify: `src/main/java/com/tattos/config/SecurityConfig.java`
- Modify: `README.md` (tabela de controle de acesso — adicionar linha Studio)

- [ ] **Step 1:** Adicionar requestMatchers: `/api/v1/studio` GET para autenticados; PATCH e POSTs restritos conforme contratos (a regra fina já está nos `@PreAuthorize`; aqui garantir que `/api/v1/studio/**` exige autenticação).
- [ ] **Step 2:** Conferir `@Tag`/`@Operation`/`@SecurityRequirement(name = "bearerAuth")` em todos os endpoints novos; subir app e conferir `/swagger-ui.html`.
- [ ] **Step 3:** `./gradlew test` — suíte completa PASS.
- [ ] **Step 4: Commit** — `git commit -am "feat(studio): seguranca por perfil e documentacao OpenAPI da vitrine"`
