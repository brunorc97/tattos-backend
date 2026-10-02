# TattOS ERP — Back‑end

> ERP distribuído para estúdios de tatuagem e artistas independentes de *body art*.
> Unifica gestão operacional (agendamentos, biossegurança) e financeira (fluxo de caixa, estoque).

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 25 (LTS) |
| Framework | Spring Boot 4.0.8 |
| Segurança | Spring Security 7 + JWT (JJWT 0.12.6) |
| Persistência | Spring Data JPA / Hibernate + MySQL 8.4 |
| Migrations | Flyway |
| Documentação | Springdoc OpenAPI 2.6 (Swagger UI) |
| Build | Gradle 9.7 |
| Containers | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Front‑end (planejado) | React 18 |

---

## Arquitetura

```
tattos-backend/
├── config/          # SecurityConfig, OpenApiConfig
├── controller/      # REST Controllers (Auth, Agendamento, Insumo, Transacao, FichaAnamnese, Usuario)
├── dto/             # Request/Response DTOs
├── exception/       # GlobalExceptionHandler + exceções de domínio
├── model/           # Entidades JPA (Usuario, Agendamento, Insumo, TransacaoFinanceira, FichaAnamnese…)
├── repository/      # Spring Data JPA Repositories
├── security/        # JwtTokenProvider, JwtAuthenticationFilter, CustomUserDetailsService
└── service/         # Regras de negócio
```

### Módulos funcionais

| Módulo | Endpoints base | Roles permitidas |
|---|---|---|
| Autenticação | `/api/auth/**` | Público |
| Agendamentos | `/api/agendamentos/**` | GESTOR, TATUADOR, CLIENTE |
| Fichas de Anamnese | `/api/fichas-anamnese/**` | GESTOR, TATUADOR, CLIENTE |
| Insumos / Estoque | `/api/estoque/**` | GESTOR, TATUADOR |
| Financeiro | `/api/financeiro/**` | GESTOR, TATUADOR |

---

## Sprints

| Sprint | Status | Descrição |
|---|---|---|
| Sprint 0 | ✅ Concluída | Setup do projeto, Docker, Gradle, configuração inicial |
| Sprint 1 | ✅ Concluída | Entidades de domínio, migrations Flyway, repositórios |
| Sprint 2 | ✅ Concluída | Controllers REST, DTOs, serviços, GlobalExceptionHandler |
| Sprint 3 | ✅ Concluída | JWT, Spring Security, testes unitários, CI/CD base |
| Sprint 4 | 🔄 Em andamento | Segurança avançada, OpenAPI/Swagger, logs estruturados, prod profile, CORS robusto |

---

## Pré‑requisitos

- **Docker** ≥ 24 e **Docker Compose** ≥ 2.20
- **Make** (opcional, para atalhos)
- Java 25 + Gradle (apenas para desenvolvimento local sem Docker)

---

## Execução via Docker (recomendado)

```bash
# Clonar o repositório
git clone https://github.com/seu-usuario/tattos-backend.git
cd tattos-backend

# Subir todos os serviços (back‑end + MySQL) – usa a plataforma nativa (ARM64) em Apple Silicon
make run
```

### Variáveis de ambiente

As variáveis abaixo podem ser sobrescritas via **`.env`** ou diretamente no `docker‑compose.yml`:

| Variável | Padrão (dev) | Descrição |
|---|---|---|
| `DB_HOST` | `db` | Host do MySQL |
| `DB_PORT` | `3306` | Porta do MySQL |
| `DB_NAME` | `tattos_db` | Nome do banco |
| `DB_USERNAME` | `root` | Usuário do banco |
| `DB_PASSWORD` | `root` | Senha do banco |
| `JWT_SECRET` | *(valor dev)* | Segredo para assinar tokens JWT (mín. 32 chars) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile ativo (`dev` ou `prod`) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Origens permitidas – pode ser uma lista separada por vírgula |

> ⚠️ **Em produção**, sempre forneça `JWT_SECRET` e `CORS_ALLOWED_ORIGINS` via variáveis de ambiente e **não** comite segredos no repositório.

---

## Execução local (sem Docker)

```bash
# Requer MySQL rodando localmente (ou use o perfil dev com H2)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

> **Dica:** crie um arquivo `.env` na raiz e exporte as variáveis antes de iniciar a aplicação.

---

## Documentação da API

Com os containers rodando, acesse:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Testando endpoints protegidos
1. `POST /api/auth/login` → obtenha o `token` JWT.
2. No Swagger UI clique em **Authorize** e cole `Bearer <token>`.
3. Explore os endpoints — as anotações OpenAPI exibem descrições, códigos de resposta e requisitos de segurança.

---

## Actuator & Healthchecks

O Spring Actuator está habilitado e expõe endpoints sob `/actuator`.

```bash
# Verificar health do serviço (usado também pelo healthcheck do Docker)
curl http://localhost:8080/actuator/health
```

O **Actuator** roda na mesma porta (8080) em desenvolvimento e na porta **8081** em produção (configurado em `application‑prod.yml`).

---

## Logs estruturados

- **Aplicação:** `logs/tattos-application.log` (JSON via Logstash encoder)
- **Auditoria de segurança:** `logs/tattos-security.log`

```bash
# Visualizar logs em tempo real
tail -f logs/tattos-application.log
tail -f logs/tattos-security.log
```

---

## Testes

```bash
# Executar todos os testes unitários
./gradlew test

# Relatório de cobertura
./gradlew test jacocoTestReport
```

---

## Makefile — Atalhos disponíveis

| Comando | Descrição |
|---|---|
| `make run` | Build + sobe todos os containers |
| `make stop` | Para e remove os containers |
| `make logs` | Exibe logs do back‑end |
| `make build` | Apenas faz o build da imagem Docker |

---

## CI/CD

O pipeline GitHub Actions (`.github/workflows/ci‑cd.yml`) executa automaticamente a cada push/PR em `main`:

1. Setup do JDK 25 e Gradle
2. Testes unitários
3. Checagem de vulnerabilidades (OWASP Dependency‑Check)
4. Build do JAR e da imagem Docker (multi‑arch: amd64 + arm64)
5. Upload de artefatos (JAR, relatórios OWASP)

---

## Segurança

- **JWT stateless** – sem sessão no servidor
- **BCrypt** para hash de senhas
- **Roles** – `TATUADOR`, `GESTOR`, `CLIENTE`
- **CORS** configurado via `CORS_ALLOWED_ORIGINS` (variável de ambiente) e aplicado em `SecurityConfig`
- **CSP, X‑Frame‑Options, HSTS** via Spring Security Headers
- **Secrets** externalizados via variáveis de ambiente (perfil `prod`)
- **Auditoria** de eventos de segurança via Logback JSON

---

## Contribuindo

1. Fork o repositório
2. Crie uma branch `feature/<nome>`
3. Rode os testes: `./gradlew test`
4. Execute o scanner de vulnerabilidades: `./gradlew dependencyCheckAnalyze`
5. Abra um Pull Request descrevendo as mudanças e incluindo screenshots (se houver UI)

---

## Roadmap (próximos passos)

- Monitoramento com Prometheus & Grafana
- Deploy em Kubernetes (Helm Chart)
- Multi‑tenant support para múltiplos estúdios
- Integração com serviços de pagamento (Stripe, PayPal)

---

## Licença

MIT © TattOS Team
