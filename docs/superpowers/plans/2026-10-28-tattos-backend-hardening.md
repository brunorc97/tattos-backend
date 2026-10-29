# TattOS Backend — Backlog de Hardening (Itens 2–13) — Plano de Implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar os 13 itens do backlog de priorização (validação centralizada, paginação por padrão, testes de integração dos controllers restantes, config segura, exceções de banco limpas, MDC/correlation-id, métricas Prometheus, eventos de domínio, exemplos OpenAPI, Dockerfile otimizado, i18n e versionamento de API).

**Architecture:** Spring Boot 4.0.8 / Java 25, Gradle, MySQL (prod) + H2 (test). Cada tarefa é independente e fecha uma lacuna real identificada na exploração do código (vários itens estavam parcialmente implementados: paginação já existe em rotas `/paginado`, Dockerfile já é multi-stage, `DataIntegrityViolationException` já é tratada).

**Tech Stack:** Spring Boot (Web, Data JPA, Security, Validation, Actuator), springdoc-openapi 2.6.0, jjwt 0.12.6, Flyway, JUnit 5 + MockMvc + H2, Micrometer + Prometheus.

**Spec:** backlog priorizado colado na conversa (itens 2–13); estado real do código levantado em exploração em 2026-10-28.

## Global Constraints

- Java 25, Spring Boot 4.0.8, Gradle via `./gradlew`.
- Testes seguem o padrão existente: `@SpringBootTest + @ActiveProfiles("test") + @Transactional`, web com `@AutoConfigureMockMvc` + `MockMvc` + `@WithMockUser`. H2 em memória (`application-test.yml`). **Sem Testcontainers, sem Mockito-only.**
- Contrato de erro existente: `ErrorResponseDTO{timestamp,status,error,message,path,fieldErrors?}` via `ErrorResponseDTO.of(...)` — manter.
- Mensagens em pt-BR em `messages.properties`; chaves novas seguem o padrão `validation.*` / `error.*`.
- Controllers usam aliases de path PT/EN — preservar todos os aliases ao modificar mappings.
- Comandos de verificação: `./gradlew test` (ou `./gradlew test --tests 'ClasseAlvo'` para rodadas focadas) e `./gradlew build`.

## Review Focus

1. **Quebra de contrato ao paginar `GET /`**: o Front-end hoje consome `List<T>` das rotas de listagem — a mudança para `Page<T>` é breaking; o plano exige manter compatibilidade via novo path + deprecação. Teste que pin: `PaginacaoCompatTest` (Task 2).
2. **Segredo JWT divergente entre namespaces** (`jwt.secret` vs `app.security.jwt-secret`): se um ambiente definir só um, a app sobe com chaves diferentes entre emissor e validador. Teste: `JwtPropertiesConsistencyTest` (Task 4).
3. **DataIntegrityViolation com mensagem bruta do MySQL** pode vazar nome de constraint/coluna. Teste: asserção de que o body 409/422 **não contém** o nome da constraint (Task 5).
4. **MDC sem usuário autenticado** (endpoints públicos, `/api/auth/login`) não pode quebrar o filtro. Teste: requisição sem token passa e log contém correlation-id (Task 6).
5. **Eventos de domínio em rollback**: transação de agendamento que falha após publicar evento não pode gerar transação financeira órfã — listeners devem ser `@TransactionalEventListener(phase = AFTER_COMMIT)`. Teste: rollback não gera transação (Task 8).

---

## 🔴 ALTA PRIORIDADE

### Task 1: Validação centralizada via MessageSource (Item 2)

**Estado real:** DTOs já têm anotações jakarta; `messages.properties` existe mas é órfão (nenhum `MessageSource` injetado); `GlobalExceptionHandler` hardcoded strings.

**Files:**
- Modify: `src/main/java/com/tattos/exception/GlobalExceptionHandler.java`
- Modify: `src/main/resources/messages.properties`
- Modify: `src/main/java/com/tattos/dto/request/*.java` (trocar mensagens literais por `{chaves}`)
- Test: `src/test/java/com/tattos/controller/ValidationMessageSourceTest.java`

**Interfaces:**
- Produces: `GlobalExceptionHandler` com construtor recebendo `MessageSource messageSource`; todas as mensagens de erro vindas de `messageSource.getMessage(key, args, locale)`.

- [ ] **Step 1: Escrever o teste que falha**

`ValidationMessageSourceTest` (`@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")`): POST `/api/insumos` com body inválido (nome em branco) → `status().isBadRequest()` e `jsonPath("$.fieldErrors.nome").value("O nome é obrigatório")` (texto vindo de `messages.properties`, não literal no DTO). Segundo teste: provocar `ResourceNotFoundException` (GET `/api/insumos/99999`) e assertar `$.message` igual ao valor da chave `error.not_found`.

- [ ] **Step 2: Rodar e confirmar falha** — `./gradlew test --tests ValidationMessageSourceTest` → FAIL (mensagens literais divergentes).

- [ ] **Step 3: Implementar** — (a) injetar `MessageSource` no `GlobalExceptionHandler` e substituir strings literais pelas chaves existentes (`error.not_found`, `error.data.integrity`, etc.); (b) nos DTOs, trocar `message = "..."` por `message = "{validation.required}"` (e demais chaves já existentes), criando chaves novas em `messages.properties` onde faltar. Spring Boot auto-configura `MessageSource` para `messages.properties` — nenhum bean extra necessário.

- [ ] **Step 4: Rodar testes** — `./gradlew test --tests ValidationMessageSourceTest --tests ValidationIntegrationTest` → PASS.

- [ ] **Step 5: Commit** — `feat(validacao): centraliza mensagens de erro em messages.properties via MessageSource`

---

### Task 2: Paginação como padrão nas listagens (Item 6)

**Estado real:** `GET /` retorna `List<T>` sem limite; `GET /paginado` (+ alias `/paged`) já retorna `Page<T>` com filtros e `@PageableDefault(size=20)`. Repositories já são `JpaRepository`.

**Files:**
- Modify: `AgendamentoController.java`, `InsumoController.java`, `TransacaoController.java` (e respectivos Services se necessário)
- Test: `src/test/java/com/tattos/controller/PaginacaoCompatTest.java`

**Decisão de contrato:** `GET /` passa a delegar ao método paginado retornando `Page<T>` (breaking, coordenado com Front) **OU** manter `List<T>` e documentar deprecação. Executar conforme escolha do usuário no review do plano; default do plano: **`GET /` retorna `Page<T>`**, removendo `/paginado` e `/paged` (o alias `/paginado` fica como redirect lógico apontando para o mesmo método, para não quebrar consumidores que já o usam).

**Interfaces:**
- Consumes: `AgendamentoService.listarPaginado(tatuadorId, clienteId, status, data, Pageable) : Page<AgendamentoResponseDTO>` (linha ~255); `InsumoService.listarPaginado(categoria, query, Pageable)` (linha ~126); `TransacaoService.listarPaginado(tipo, tatuadorId, status, inicio, fim, Pageable)` (linha ~196).
- Produces: `GET /api/agendamentos`, `GET /api/estoque`, `GET /api/financeiro/transacoes` → `ResponseEntity<Page<...>>` com `@PageableDefault(size=20, sort=...)` e `@ParameterObject Pageable`.

- [ ] **Step 1: Escrever o teste que falha**

`PaginacaoCompatTest`: semear 25 insumos; `GET /api/insumos?page=0&size=10` → `jsonPath("$.content.length()").value(10)`, `jsonPath("$.totalElements").value(25)`, `jsonPath("$.totalPages").value(3)`; `GET /api/insumos?sort=nome,desc` → primeiro item é o de maior nome. Repetir asserção de `Page` para `/api/agendamentos` e `/api/financeiro/transacoes` (1 caso cada).

- [ ] **Step 2: Rodar e confirmar falha** — `$.content` não existe (retorna array) → FAIL.

- [ ] **Step 3: Implementar** — nos 3 controllers, trocar o retorno de `GET /` (e `/paginado`, `/paged` via mesmo handler com múltiplos paths em `@GetMapping({"", "/paginado", "/paged"})`) para `Page<...>` delegando ao `listarPaginado` existente. Remover/adaptar o método `listar(...)` não paginado do service se ficar sem chamadores.

- [ ] **Step 4: Rodar testes** — `./gradlew test --tests PaginacaoCompatTest` + suite completa → PASS (ajustar testes existentes que esperavam array).

- [ ] **Step 5: Commit** — `feat(paginacao): torna Page<T> o retorno padrão das rotas de listagem`

---

### Task 3: Testes de integração dos 4 controllers restantes (Item 4)

**Files:**
- Test: `src/test/java/com/tattos/controller/TransacaoControllerTest.java`, `ComissaoControllerTest.java`, `OrcamentoControllerTest.java`, `ProjetoControllerTest.java`

**Interfaces:**
- Consumes: padrão de seed de `TransacaoServiceTest` (`@BeforeEach` com `deleteAll()` + criação de `Tatuador` com comissão 60%).

- [ ] **Step 1: TransacaoControllerTest** — cobrir: `GET /api/financeiro/transacoes` (200 + paginação), `GET /{id}` (200 e 404), `POST /transacoes` (201/200 com `@WithMockUser(roles="GESTOR")`; 400 com body inválido), `GET /balanco`, `GET /repasses/{tatuadorId}`, `GET /comissoes/{tatuadorId}/fechamento`, `DELETE /{id}` (204/200 e 404), e 403 com role inadequada.

- [ ] **Step 2: ComissaoControllerTest** — `GET /api/comissoes/tatuador/{id}/fechamento/export` → 200, `contentType` compatível com CSV e corpo contendo cabeçalho; 404 para tatuador inexistente.

- [ ] **Step 3: OrcamentoControllerTest** — `GET /api/orcamentos` 200; `POST /api/orcamentos` válido → 200/201; inválido → 400 com `fieldErrors`.

- [ ] **Step 4: ProjetoControllerTest** — `GET /api/projetos` 200; `POST /api/projetos` 200/201; `POST /{id}/aprovar` → 200 e efeito colateral (projeto aprovado/kanban) verificado via repository; 404 para id inexistente.

- [ ] **Step 5: Rodar** — `./gradlew test --tests '*ControllerTest'` → PASS.

- [ ] **Step 6: Commit** — `test(integracao): adiciona suites de controller para transacoes, comissoes, orcamentos e projetos`

---

### Task 4: Configurações e segredos seguros (Item 9)

**Estado real:** `AppSecurityProperties` (`@ConfigurationProperties(prefix="app.security") @Validated`, `jwtSecret @NotBlank @Size(min=32)`, `jwtExpiration @Min(300000)`) **existe mas não é usado** — `JwtTokenProvider` lê `@Value("${jwt.secret}")`. Dois namespaces duplicados.

**Files:**
- Modify: `src/main/java/com/tattos/security/JwtTokenProvider.java`
- Modify: `src/main/java/com/tattos/config/AppSecurityProperties.java` (adicionar `@ConfigurationPropertiesScan` ou `@EnableConfigurationProperties` se ausente)
- Modify: `src/main/resources/application.yml`, `application-prod.yml` (remover bloco `jwt.*` duplicado, manter só `app.security.*`)
- Test: `src/test/java/com/tattos/security/JwtPropertiesConsistencyTest.java`

**Interfaces:**
- Produces: `JwtTokenProvider(AppSecurityProperties props)` via construtor; `props.getJwtSecret()`, `props.getJwtExpiration()`.

- [ ] **Step 1: Teste que falha** — `JwtPropertiesConsistencyTest`: contexto sobe com `app.security.jwt-secret` definido e `jwt.secret` **ausente**; gera token via `JwtTokenProvider` e valida (`validateToken` true). Hoje falha porque o provider exige `jwt.secret`.

- [ ] **Step 2: Rodar e confirmar falha.**

- [ ] **Step 3: Implementar** — refatorar `JwtTokenProvider` para receber `AppSecurityProperties` no construtor; remover bloco `jwt:` de `application.yml`/`application-prod.yml`; garantir `app.security.jwt-secret: ${JWT_SECRET}` (sem default) e `jwt-expiration: ${JWT_EXPIRATION:86400000}` em ambos os perfis; atualizar `SecurityAndAuditingTest`/configs de teste que usam a chave antiga.

- [ ] **Step 4: Rodar** — `./gradlew test` → PASS; `./gradlew bootRun` falha rápido sem `JWT_SECRET` (validação `@NotBlank`).

- [ ] **Step 5: Commit** — `refactor(config): unifica config JWT em AppSecurityProperties validado`

---

## 🟡 MÉDIA PRIORIDADE

### Task 5: DataIntegrityViolationException refinada (Item 3)

**Estado real:** handler existe mas retorna mensagem genérica; precisa distinguir duplicidade (409) de exclusão com vínculo FK (422) e nunca vazar nome de constraint.

**Files:**
- Modify: `src/main/java/com/tattos/exception/GlobalExceptionHandler.java` (handler de `DataIntegrityViolationException`)
- Modify: `src/main/resources/messages.properties` (`error.duplicate`, `error.foreign_key`)
- Test: `src/test/java/com/tattos/exception/DataIntegrityHandlerTest.java`

- [ ] **Step 1: Teste que falha** — (a) cadastrar 2 usuários com mesmo e-mail → 409 com `$.message` = texto da chave `error.duplicate` e body **não contém** `Duplicate entry` nem nome da constraint; (b) excluir entidade com vínculo (ex.: tatuador com agendamento) → 422 com mensagem `error.foreign_key`.

- [ ] **Step 2: Rodar e confirmar falha.**

- [ ] **Step 3: Implementar** — no handler, inspecionar `ex.getCause()`/`getMessage()`: se contém `Duplicate entry`/`DuplicateKeyException` → 409 + `error.duplicate`; se contém `foreign key constraint`/`Cannot delete or update a parent row` → 422 + `error.foreign_key`; senão → 409 + `error.data.integrity`. Logar a mensagem bruta apenas no log (WARN), nunca no body.

- [ ] **Step 4: Rodar** — `./gradlew test --tests DataIntegrityHandlerTest` → PASS.

- [ ] **Step 5: Commit** — `feat(exceptions): traduz DataIntegrityViolation em 409/422 sem vazar detalhes do banco`

---

### Task 6: Logs estruturados com MDC / X-Correlation-ID (Item 14)

**Files:**
- Create: `src/main/java/com/tattos/config/CorrelationIdFilter.java` (`OncePerRequestFilter`, `@Order(Ordered.HIGHEST_PRECEDENCE)`)
- Modify: `src/main/resources/application.yml` (`logging.pattern.level`/console incluindo `%X{correlationId}` e `%X{user}`)
- Test: `src/test/java/com/tattos/config/CorrelationIdFilterTest.java`

**Interfaces:**
- Produces: header `X-Correlation-ID` (lido se presente, senão `UUID.randomUUID()`); MDC keys `correlationId` e `user` (username do `SecurityContextHolder`, `anonymous` se ausente); resposta ecoa o header.

- [ ] **Step 1: Teste que falha** — MockMvc: (a) GET autenticado sem header → resposta contém `X-Correlation-ID` (UUID válido); (b) GET com `X-Correlation-ID: abc-123` → resposta ecoa `abc-123`; (c) endpoint público sem token → 200/401 normal, sem exceção (MDC limpo via `finally { MDC.clear() }`).

- [ ] **Step 2: Rodar e confirmar falha** (header ausente na resposta).

- [ ] **Step 3: Implementar** o filtro + padrão de log.

- [ ] **Step 4: Rodar** — `./gradlew test --tests CorrelationIdFilterTest` → PASS; smoke: `./gradlew bootRun` e conferir `correlationId` no console.

- [ ] **Step 5: Commit** — `feat(observabilidade): adiciona X-Correlation-ID e usuario ao MDC`

---

### Task 7: Métricas customizadas Actuator + Prometheus (Item 5)

**Files:**
- Modify: `build.gradle` (adicionar `runtimeOnly 'io.micrometer:micrometer-registry-prometheus'`)
- Modify: `application-prod.yml` (expor `prometheus` em `management.endpoints.web.exposure.include`)
- Create: `src/main/java/com/tattos/metrics/BusinessMetricsService.java`
- Test: `src/test/java/com/tattos/metrics/BusinessMetricsTest.java`

**Interfaces:**
- Produces: `BusinessMetricsService(MeterRegistry, AgendamentoRepository, TransacaoFinanceiraRepository)` registrando gauges: `tattos.agendamentos.status{status=...}` (count por `StatusAgendamento`), `tattos.faturamento.diario` (soma de transações RECEITA do dia), `tattos.sinais.taxa_pagamento` (sinais pagos / total com sinal). Endpoint `/actuator/prometheus`.

- [ ] **Step 1: Teste que falha** — `BusinessMetricsTest` (`@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")` com actuator exposto no test yml): seed de 2 agendamentos (1 CONFIRMADO, 1 com sinal pago) → `GET /actuator/prometheus` 200 contendo `tattos_agendamentos_status` e `tattos_sinais_taxa_pagamento`.

- [ ] **Step 2: Rodar e confirmar falha** (404/503 sem registry).

- [ ] **Step 3: Implementar** — dependência + `@Configuration`/`@Component` com gauges registrados via `MeterRegistry.gauge(..., ToDoubleFunction)` consultando os repositories (queries `countByStatus`, soma por data — adicionar métodos de repository se necessário).

- [ ] **Step 4: Rodar** — `./gradlew test --tests BusinessMetricsTest` → PASS.

- [ ] **Step 5: Commit** — `feat(metricas): expoe metricas de negocio via Actuator/Prometheus`

---

### Task 8: Eventos de domínio para transações automáticas e lembretes (Item 8)

**Estado real:** `AgendamentoService.confirmarPagamentoSinal` (~l.114) e `atualizarStatus` → REALIZADO (~l.195) constroem e salvam `TransacaoFinanceira` diretamente via `TransacaoFinanceiraRepository`; `NotificacaoSchedulerService` só loga. Zero uso de `ApplicationEventPublisher`.

**Files:**
- Create: `src/main/java/com/tattos/event/SinalPagoEvent.java` (record com `agendamentoId`), `AgendamentoRealizadoEvent.java`, `LembreteAgendamentoEvent.java`
- Create: `src/main/java/com/tattos/event/listener/TransacaoAutomaticaListener.java`, `NotificacaoListener.java`
- Modify: `src/main/java/com/tattos/service/AgendamentoService.java` (publicar eventos em vez de salvar transação; remover injeção de `TransacaoFinanceiraRepository`)
- Modify: `src/main/java/com/tattos/service/NotificacaoSchedulerService.java` (publicar `LembreteAgendamentoEvent`/alerta de estoque em vez de logar direto)
- Test: `src/test/java/com/tattos/event/DomainEventsTest.java`

**Interfaces:**
- Produces: `record SinalPagoEvent(Long agendamentoId)`, `record AgendamentoRealizadoEvent(Long agendamentoId)`; listeners `@TransactionalEventListener(phase = AFTER_COMMIT)` gerando as transações com a mesma lógica atual (categoria "Sinal Reserva" / "Procedimento Tattoo", `calcularRepasse()`).

- [ ] **Step 1: Teste que falha** — `DomainEventsTest`: (a) confirmar sinal → após commit existe `TransacaoFinanceira` de categoria "Sinal Reserva" (comportamento preservado); (b) **rollback**: forçar exceção após `confirmarPagamentoSinal` dentro de transação → nenhuma transação criada; (c) `AgendamentoService` não referencia mais `TransacaoFinanceiraRepository` (asserção via teste de arquitetura simples ou verificação manual no review).

- [ ] **Step 2: Rodar e confirmar falha** de (b) (hoje a transação é criada na mesma TX — o teste de rollback passa? não: o comportamento a pinar é o evento; o teste (a) falha se removermos a lógica antes de criar o listener — escrever (a) contra o novo caminho).

- [ ] **Step 3: Implementar** — criar events + listeners movendo o bloco de criação de transação para `TransacaoAutomaticaListener`; `AgendamentoService` injeta `ApplicationEventPublisher` e publica; scheduler publica eventos e `NotificacaoListener` (não transacional, `@EventListener`) faz o log/entrega.

- [ ] **Step 4: Rodar** — `./gradlew test --tests DomainEventsTest --tests AgendamentoServiceTest --tests NotificacaoSchedulerServiceTest` → PASS.

- [ ] **Step 5: Commit** — `refactor(eventos): desacopla geracao de transacoes e lembretes via Spring Domain Events`

---

### Task 9: Exemplos OpenAPI nos DTOs (Item 7)

**Files:**
- Modify: todos os DTOs em `src/main/java/com/tattos/dto/request/` e `dto/response/` (adicionar `@Schema(example = "...")` por campo)
- Test: `src/test/java/com/tattos/config/OpenApiExamplesTest.java`

- [ ] **Step 1: Teste que falha** — `OpenApiExamplesTest`: `GET /v3/api-docs` (MockMvc) → `jsonPath("$.components.schemas.AgendamentoRequestDTO.properties.dataHora.example").exists()` e idem para `InsumoRequestDTO.nome`, `TransacaoRequestDTO.valor`.

- [ ] **Step 2: Rodar e confirmar falha.**

- [ ] **Step 3: Implementar** — anotar campos com `@Schema(example = ...)` (ex.: `dataHora` → `"2026-11-15T14:00:00"`, `valor` → `"1500.00"`, `nome` → `"Agulha 3RL"`, e-mail → `"cliente@exemplo.com"`), incluindo `description` curta onde ausente.

- [ ] **Step 4: Rodar** — `./gradlew test --tests OpenApiExamplesTest` → PASS.

- [ ] **Step 5: Commit** — `docs(openapi): adiciona exemplos de payload em todos os DTOs`

---

### Task 10: Otimização final do Dockerfile (Item 11)

**Estado real:** já é multi-stage (builder `eclipse-temurin:25-jdk` → runtime `25-jre`, usuário não-root). Lacunas: sem `HEALTHCHECK`, sem `JAVA_OPTS`/`SPRING_PROFILES_ACTIVE` por env, camada de jar única (sem layered jar).

**Files:**
- Modify: `Dockerfile`
- Modify: `build.gradle` (habilitar `bootJar { layered() }` se aplicável)

- [ ] **Step 1: Verificação** — `docker build -t tattos:test .` e registrar tamanho da imagem antes/depois.

- [ ] **Step 2: Implementar** — (a) layered jar: extrair com `java -Djarmode=layertools -jar app.jar extract` no builder e copiar `dependencies/`, `spring-boot-loader/`, `snapshot-dependencies/`, `application/` em camadas separadas no runtime; (b) `HEALTHCHECK --interval=30s --timeout=5s CMD curl -f http://localhost:8080/actuator/health || exit 1` (garantir `curl`/`wget` disponível ou usar `java` com socket simples — escolher `wget` do temurin ou instalar curl na camada runtime); (c) `ENV JAVA_OPTS=""` e entrypoint `java $JAVA_OPTS ... -jar app.jar`.

- [ ] **Step 3: Verificar** — rebuild: tamanho menor ou igual e `docker run` sobe saudável (`docker inspect --format '{{.State.Health.Status}}'`).

- [ ] **Step 4: Commit** — `build(docker): layered jar, healthcheck e JAVA_OPTS no Dockerfile`

---

## 🟢 BAIXA PRIORIDADE

### Task 11: Internacionalização i18n (Item 12)

**Files:**
- Create: `src/main/resources/messages_en.properties` (tradução das chaves de `messages.properties`)
- Create: `src/main/java/com/tattos/config/LocaleConfig.java` (`LocaleResolver` baseado em `Accept-Language`, default `pt-BR`)
- Test: `src/test/java/com/tattos/i18n/I18nMessagesTest.java`

- [ ] **Step 1: Teste que falha** — POST inválido com header `Accept-Language: en` → `fieldErrors.nome` = "Name is required" (ou equivalente); sem header → pt-BR.

- [ ] **Step 2–5:** rodar/falhar → implementar (LocaleConfig + arquivo EN; o `GlobalExceptionHandler` da Task 1 já resolve por `LocaleContextHolder.getLocale()`) → rodar/passar → commit `feat(i18n): suporte pt-BR/en via Accept-Language`.

---

### Task 12: Versionamento de API /api/v1 (Item 13)

**Files:**
- Modify: todos os `@RequestMapping` dos controllers (prefixar `/api/v1/...` mantendo aliases legados)
- Modify: `SecurityConfig.java` (matchers), `OpenApiConfig.java` (doc v1)
- Test: `src/test/java/com/tattos/controller/ApiVersioningTest.java`

**Decisão:** manter paths legados funcionando por um ciclo (aliases) — ex.: `@RequestMapping({"/api/v1/agendamentos", "/api/agendamentos", "/api/appointments", "/api/sessions"})`.

- [ ] **Step 1: Teste que falha** — `GET /api/v1/agendamentos` → 200 (com auth mockada); `GET /api/agendamentos` → 200 (legado).

- [ ] **Step 2–5:** rodar/falhar → implementar prefixos + ajustar `SecurityConfig` e springdoc → `./gradlew test` completo → commit `feat(api): introduz versionamento /api/v1 mantendo aliases legados`.

---

## Ordem sugerida e dependências

1. Task 1 → destrava Tasks 5 e 11 (ambas dependem das chaves em `messages.properties` e do `MessageSource`).
2. Task 2 → Task 12 toca os mesmos mappings; executar 2 antes de 12.
3. Tasks 3, 4, 6, 7, 9, 10 independentes — podem paralelizar.
4. Task 8 depende do estado atual de `AgendamentoService`; Task 3 (testes de controller) pode ser escrita antes ou depois, mas rodar a suite completa após a 8.

Ordem recomendada de execução: **1 → 4 → 2 → 5 → 3 → 6 → 7 → 8 → 9 → 10 → 11 → 12**.
