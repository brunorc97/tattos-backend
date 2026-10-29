TattOS ERP - Sistema de Gestão Integrada para Estúdios de Tatuagem e Body Art
Desenvolvido para estúdios de tatuagem e profissionais independentes de tatuagem e body art.

O projeto surgiu a partir da necessidade de centralizar processos que normalmente são realizados de forma descentralizada, utilizando planilhas, aplicativos de mensagens e controles manuais.

A proposta do TattOS é reunir, em uma única plataforma, recursos para:
* gerenciamento de agendamentos;
* gerenciamento de clientes e profissionais;
* fichas de anamnese e informações relacionadas à biossegurança;
* controle de insumos e estoque;
* controle financeiro;
* autenticação e autorização por perfil de usuário;
* organização dos processos administrativos do estúdio.


Principais módulos

Módulo	Descrição	Status
Autenticação	Login, registro e autenticação via JWT	✅
Agendamentos	Controle de sessões e horários, solicitações de agendamento	✅
Anamnese	Fichas de anamnese dos clientes com registro de assinatura	✅
Orçamentos	Geração e gestão de orçamentos	✅
Projetos / Kanban	Gestão de projetos em quadro Kanban, aprovação e sinal	✅
Estoque	Controle de insumos utilizados pelo estúdio	✅
Financeiro	Receitas, despesas, taxas de operadora e valor líquido	✅
Adiantamentos	Adiantamentos a tatuadores com dedução de comissão	✅
Comissões	Pagamento de comissões com recibo em PDF	✅
Cupons	Cupons de desconto com validação e controle de uso	✅
Clientes (CRM)	Cadastro de clientes, contatos e fidelização	✅
Aftercare	Acompanhamento pós-procedimento com progresso de cicatrização	✅
Bancadas	Controle de bancadas/estações de trabalho	✅
Arquivos	Upload e gestão de arquivos	✅
Google Calendar	Integração com Google Calendar	✅
Usuários	Gestão de usuários e perfis de acesso	✅
API REST	Endpoints documentados via OpenAPI	✅
Segurança	JWT, BCrypt, CORS e Security Headers	✅
Testes	Testes automatizados	✅
Docker	Ambiente conteinerizado	✅
CI/CD	Pipeline automatizado	✅
Front-end	Interface web React	🔄 Em andamento

 Arquitetura

O back-end foi desenvolvido seguindo uma arquitetura baseada na separação de responsabilidades entre Controllers, Services, Repositories e Models, utilizando o padrão de desenvolvimento oferecido pelo ecossistema Spring.

                        ┌─────────────────────┐
                        │     Cliente / UI    │
                        │   Front-end React   │
                        └──────────┬──────────┘
                                   │
                                   │ HTTP / REST
                                   ▼
                        ┌─────────────────────┐
                        │    REST Controller  │
                        │                     │
                        │ Auth / Agendamento  │
                        │ Estoque / Financeiro│
                        │ Anamnese / Usuários │
                        │ Orçamento / Projeto │
                        │ Cupons / Comissões  │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │       Service       │
                        │                     │
                        │ Regras de negócio   │
                        │ Validações          │
                        │ Processamentos      │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │     Repository      │
                        │                     │
                        │ Spring Data JPA     │
                        │ Hibernate           │
                        └──────────┬──────────┘
                                   │
                                   ▼
                        ┌─────────────────────┐
                        │      MySQL 8.4      │
                        │                     │
                        │ Banco de dados      │
                        └─────────────────────┘

Organização do código

src/
└── main/
    └── java/
        └── .../
            ├── config/
            │   ├── SecurityConfig
            │   └── OpenApiConfig
            │
            ├── controller/
            │   ├── AuthController
            │   ├── AgendamentoController
            │   ├── SolicitacaoAgendamentoController
            │   ├── FichaAnamneseController
            │   ├── OrcamentoController
            │   ├── ProjetoController
            │   ├── KanbanCardController
            │   ├── InsumoController
            │   ├── TransacaoController
            │   ├── TaxaOperadoraController
            │   ├── AdiantamentoController
            │   ├── ComissaoController / PagamentoComissaoController
            │   ├── CupomController
            │   ├── ClienteCrmController
            │   ├── AftercareController
            │   ├── BancadaController
            │   ├── FileController
            │   ├── GoogleCalendarController
            │   └── UsuarioController
            │
            ├── dto/
            │   ├── request/
            │   └── response/
            │
            ├── exception/
            │   └── GlobalExceptionHandler
            │
            ├── model/
            │   ├── Usuario / Cliente / Tatuador / Gestor
            │   ├── Agendamento / SolicitacaoAgendamento
            │   ├── FichaAnamnese / AssinaturaLog
            │   ├── Orcamento / Projeto / KanbanCard
            │   ├── Insumo
            │   ├── TransacaoFinanceira / TaxaOperadora
            │   ├── Adiantamento (ver pacote financeiro)
            │   ├── PagamentoComissao / PagamentoComissaoItem
            │   ├── Cupom
            │   ├── Aftercare
            │   ├── Arquivo
            │   ├── GoogleAccount
            │   └── enums (StatusAgendamento, StatusTransacao, UserRole, ...)
            │
            ├── repository/
            │
            ├── security/
            │   ├── JwtTokenProvider
            │   ├── JwtAuthenticationFilter
            │   └── CustomUserDetailsService
            │
            └── service/

⸻

Stack tecnológica

Categoria	Tecnologia
Linguagem	Java 25 LTS
Framework	Spring Boot 4.0.8
Segurança	Spring Security 7
Autenticação	JWT / JJWT 0.12.6
Persistência	Spring Data JPA
ORM	Hibernate
Banco de dados	MySQL 8.4
Migrations	Flyway
Documentação da API	Springdoc OpenAPI / Swagger
Build	Gradle 9.7
Containerização	Docker
Orquestração local	Docker Compose
Testes	JUnit / Spring Test
Cobertura	JaCoCo
CI/CD	GitHub Actions
Front-end	React — em desenvolvimento


Segurança

A segurança é um dos componentes centrais do TattOS.
A API utiliza autenticação stateless baseada em JWT, permitindo que cada requisição protegida seja validada através do token de autenticação.

Recursos implementados

* Autenticação via JWT;
* Senhas armazenadas utilizando BCrypt;
* Controle de acesso baseado em roles;
* Spring Security;
* CORS configurável;
* Security Headers;
* CSP;
* X-Frame-Options;
* HSTS;
* Auditoria de eventos de segurança;
* Segredos externos através de variáveis de ambiente.

Perfis de acesso

                    ┌───────────────┐
                    │    GESTOR     │
                    └───────┬───────┘
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
          Usuários       Estoque      Financeiro
              │
              ▼
         Agendamentos
                    ┌───────────────┐
                    │   TATUADOR    │
                    └───────┬───────┘
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
        Agendamentos     Anamnese      Estoque
                    ┌───────────────┐
                    │    CLIENTE    │
                    └───────┬───────┘
                            │
                 ┌──────────┴──────────┐
                 ▼                     ▼
           Agendamentos             Anamnese

⸻

Controle de acesso

Módulo	Endpoint base	GESTOR	TATUADOR	CLIENTE
Autenticação	/api/auth/**	✅	✅	✅
Agendamentos	/api/agendamentos/**	✅	✅	✅
Solicitações de agendamento	/api/v1/solicitacoes-agendamento/**	✅	✅	✅
Anamnese	/api/fichas-anamnese/**	✅	✅	✅
Orçamentos	/api/orcamentos/**	✅	✅	✅
Projetos / Kanban	/api/projetos/**, /api/kanban/**	✅	✅	✅
Aftercare	/api/aftercare/**	✅	✅	✅
Clientes (CRM)	/api/v1/clientes/**	✅	✅	❌
Estoque	/api/estoque/**	✅	✅	❌
Financeiro	/api/financeiro/**	✅	✅	❌
Adiantamentos	/api/v1/financeiro/adiantamentos/**	✅	❌	❌
Comissões	/api/comissoes/**	✅	❌	❌
Taxas de operadora	/api/v1/financeiro/taxas-operadoras/**	✅	❌	❌
Cupons	/api/v1/cupons/**	✅	❌	❌
Bancadas	/api/v1/bancadas/**	✅	✅	❌
Arquivos	/api/v1/files/**	✅	✅	✅
Google Calendar	/api/google-calendar/**	✅	✅	❌
Usuários	/api/usuarios/**	✅	❌	❌

⸻

Banco de dados

O projeto utiliza MySQL 8.4 como banco de dados relacional.

A evolução do schema é controlada pelo Flyway, permitindo versionar alterações estruturais do banco juntamente com o código da aplicação.

                    TattOS API
                        │
                        ▼
                Spring Data JPA
                        │
                        ▼
                    Hibernate
                        │
                        ▼
                    MySQL 8.4
                        ▲
                        │
                     Flyway
                 Database Migrations

Principais entidades

* Usuario / Cliente / Tatuador / Gestor
* Agendamento / SolicitacaoAgendamento
* FichaAnamnese / AssinaturaLog
* Orcamento / Projeto / KanbanCard
* Insumo
* TransacaoFinanceira / TaxaOperadora / TaxaOperadoraParcela
* PagamentoComissao / PagamentoComissaoItem
* Cupom
* Aftercare / TrabalhoRealizado
* Arquivo
* GoogleAccount

O schema é versionado pelo Flyway (migrações V1 a V27).

⸻

Evolução do projeto

O desenvolvimento foi organizado em sprints, permitindo a implementação incremental das funcionalidades.

Sprint	Objetivo	Status
Sprint 0	Setup do projeto, Gradle, Docker e estrutura inicial	✅
Sprint 1	Modelagem das entidades, banco e migrations	✅
Sprint 2	Controllers, DTOs, Services e tratamento de exceções	✅
Sprint 3	Autenticação JWT, Spring Security e testes	✅
Sprint 4	OpenAPI, segurança avançada, logs e configuração de produção	✅
Sprint 5	Orçamentos, projetos e quadro Kanban	✅
Sprint 6	Solicitações de agendamento, CRM de clientes e aftercare	✅
Sprint 7	Financeiro avançado: taxas de operadora, adiantamentos, comissões e cupons	✅
Sprint 8	Integração Google Calendar, upload de arquivos e recibos em PDF	✅

⸻

Como executar

Pré-requisitos

Para executar o projeto utilizando Docker:

* Docker 24+
  * **macOS**: instalar o [Docker Desktop](https://www.docker.com/products/docker-desktop/)
  * **Linux**: instalar o [Docker Engine](https://docs.docker.com/engine/install/)
* Docker Compose 2.20+ (já incluído no Docker Desktop; no Linux, instalar o [plugin Compose](https://docs.docker.com/compose/install/linux/)) — o Makefile detecta automaticamente `docker compose` (v2) ou `docker-compose` (v1)
* Git
* Make — opcional, utilizado para atalhos

O projeto também pode ser executado localmente utilizando Java 25 e Gradle.

⸻

1. Clonar o projeto

git clone https://github.com/brunocorrea-97/tattos-backend.git
cd tattos-backend

⸻

2. Executar com Docker

A forma recomendada para avaliação é utilizar o Docker Compose.

make run

O comando realiza o build da aplicação e inicializa os containers necessários.

Sem Make

Caso o ambiente não possua o make instalado:

docker compose up -d --build

⸻

Containers

Após a inicialização, a aplicação será executada através dos serviços definidos no Docker Compose.

┌──────────────────────────────────────┐
│             Docker Compose           │
│                                      │
│  ┌────────────────┐                  │
│  │ TattOS Backend │                  │
│  │   Spring Boot  │                  │
│  │     :8080      │                  │
│  └───────┬────────┘                  │
│          │                            │
│          ▼                            │
│  ┌────────────────┐                  │
│  │     MySQL      │                  │
│  │      :3306     │                  │
│  └────────────────┘                  │
│                                      │
└──────────────────────────────────────┘

⸻

Variáveis de ambiente

As principais configurações podem ser fornecidas através de variáveis de ambiente.

Variável	Padrão local	Descrição
DB_HOST	db	Host do MySQL
DB_PORT	3306	Porta do MySQL
DB_NAME	tattos_db	Nome do banco
DB_USERNAME	root (somente local)	Usuário do banco
DB_PASSWORD	root (somente local)	Senha do banco
JWT_SECRET	Chave local de desenvolvimento	Chave para assinatura do JWT
SPRING_PROFILES_ACTIVE	dev	Profile da aplicação
CORS_ALLOWED_ORIGINS	http://localhost:3000	Origens autorizadas

O Docker Compose usa os valores locais acima para facilitar o desenvolvimento sem arquivo `.env`. Em produção, defina `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` seguros no ambiente ou em `.env`; nunca use os valores padrão locais.

⸻

 Documentação da API

A API utiliza OpenAPI para documentação dos endpoints.

Com a aplicação em execução:

Swagger UI

http://localhost:8080/swagger-ui.html

OpenAPI JSON

http://localhost:8080/v3/api-docs

O Swagger permite visualizar e testar os endpoints diretamente pelo navegador.

⸻

 Autenticação

Para acessar endpoints protegidos:
1. Realizar login: POST /api/auth/login
Exemplo:
{
  "email": "admin@tattos.com",
  "password": "admin123"
}

2. Receber o JWT
A API retorna um token de autenticação.

3. Autorizar no Swagger
No Swagger UI:

Authorize
    ↓
Bearer <SEU_TOKEN>

Depois disso, os endpoints protegidos podem ser executados diretamente pela interface do Swagger.

⸻

Contas para avaliação
O banco de desenvolvimento possui usuários previamente cadastrados para facilitar a avaliação do projeto.

As credenciais abaixo são exclusivamente para ambiente acadêmico/desenvolvimento.

| ID | Nome / Apelido | E-mail de Login | Perfil / Cargo | Senha (Plain) | Senha Criptografada (BCrypt) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | Administrador Gestor | `admin@tattos.com` | **GESTOR** (Gestor Executivo) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **2** | Carlos Gerente | `gerente@tattos.com` | **GESTOR** (Gerente de Operações) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **3** | Mãozinha Leve | `maozinha@tattos.com` | **TATUADOR** (Microrealismo & Fine Line) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **4** | Bracinho Curto | `bracinhocurto@tattos.com` | **TATUADOR** (Old School & Bold Line) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **5** | Agulha de Ouro | `agulhadeouro@tattos.com` | **TATUADOR** (Realismo Preto e Cinza) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **6** | Treme-Treme | `tremetreme@tattos.com` | **TATUADOR** (Tribal & Geek) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **7** | Carlos Silva | `cliente@tattos.com` | **CLIENTE** | `cliente123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **8** | Mariana Costa | `mariana@email.com` | **CLIENTE** | `cliente123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
| **9** | Roberto "Bob" Mendes | `bob@email.com` | **CLIENTE** | `cliente123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |


⸻

Testes - Os testes automatizados podem ser executados através do Gradle Wrapper.

Executar testes
./gradlew test

Gerar relatório de cobertura
./gradlew test jacocoTestReport

O relatório de cobertura será disponibilizado no diretório:
build/reports/jacoco/test/html/




Health Check

A aplicação disponibiliza um endpoint de monitoramento através do Spring Actuator.

GET /actuator/health

Ou:

curl http://localhost:8080/actuator/health

Resposta esperada:

{
  "status": "UP"
}

Esse endpoint também pode ser utilizado pelos mecanismos de healthcheck do ambiente Docker.

⸻

Logs

A aplicação utiliza logs estruturados para facilitar diagnóstico e auditoria.

Principais arquivos:

logs/
├── tattos-application.log
└── tattos-security.log

Para acompanhar os logs em tempo real:

tail -f logs/tattos-application.log

ou:

tail -f logs/tattos-security.log

⸻

Makefile

O projeto possui um Makefile para simplificar os comandos mais utilizados durante o desenvolvimento.

Comando	Descrição
make help	Exibe todos os comandos disponíveis
make run	Build + inicia os containers
make run-logs	Inicia os containers exibindo os logs
make stop	Para e remove os containers
make restart	Reinicia os serviços
make logs	Exibe os logs
make logs-app	Exibe os logs da aplicação
make logs-db	Exibe os logs do MySQL
make db	Acessa o container do banco
make build	Realiza o build
make test	Executa os testes
make clean	Limpa artefatos de build

Para visualizar os comandos:

make help

Caso o sistema não possua o make, os comandos Docker podem ser executados diretamente utilizando docker compose.

⸻

CI/CD

O projeto possui integração contínua utilizando GitHub Actions.

O pipeline é executado automaticamente em eventos configurados no repositório, como push e pull request.

Pipeline

        Push / Pull Request
                │
                ▼
        ┌───────────────┐
        │ GitHub Actions│
        └───────┬───────┘
                │
        ┌───────▼───────┐
        │ Setup Java 25 │
        └───────┬───────┘
                │
        ┌───────▼───────┐
        │     Tests     │
        └───────┬───────┘
                │
        ┌───────▼────────────┐
        │ Security / OWASP   │
        └───────┬────────────┘
                │
        ┌───────▼──────────┐
        │   Build JAR      │
        └───────┬──────────┘
                │
        ┌───────▼──────────┐
        │ Docker Image     │
        └──────────────────┘

⸻

Qualidade e boas práticas

Durante o desenvolvimento foram aplicados conceitos de:

* Arquitetura em camadas;
* Separação de responsabilidades;
* DTOs para comunicação da API;
* Tratamento global de exceções;
* Validação de dados;
* Versionamento de banco;
* Autenticação e autorização;
* Testes automatizados;
* Conteinerização;
* Integração contínua;
* Documentação de API;
* Externalização de configurações;
* Controle de versão com Git.

⸻

Roadmap

Funcionalidades planejadas para futuras versões:

* Conclusão do front-end em React;
* Dashboard gerencial;
* Relatórios financeiros;
* Alertas de estoque mínimo;
* Interface responsiva;
* Sistema de notificações e lembretes por e-mail;
* Integração com meios de pagamento;
* Monitoramento com Prometheus e Grafana (métricas já exportadas via Actuator);
* Deploy em ambiente cloud;
* Orquestração com Kubernetes;
* Suporte multiestúdio / multi-tenant;
* Integração com WhatsApp (planejada para futuro).

⸻

Conceitos aplicados

O projeto utiliza conhecimentos relacionados às seguintes áreas:

                    TattOS ERP
                        │
        ┌───────────────┼────────────────┐
        │               │                │
        ▼               ▼                ▼
 Engenharia         Backend           Banco de Dados
 de Software
        │               │                │
        ├─ Arquitetura  ├─ REST API      ├─ MySQL
        ├─ Git          ├─ Spring Boot   ├─ JPA
        ├─ CI/CD        ├─ Security      └─ Flyway
        └─ Testes       └─ JWT

⸻

Tecnologias principais:
Java • Spring Boot • Spring Security • JWT • JPA • Hibernate • MySQL • Flyway • Docker • Gradle • GitHub Actions

## Aftercare CRM Panel

The **Aftercare** module adds a new resource to manage post‑procedure follow‑up for clients.

### Endpoints
| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/api/aftercare` | Create a new after‑care record. Requires `clienteId` and `agendamentoId` as request parameters, optional `notes`.
| `GET` | `/api/aftercare/{id}` | Retrieve a specific after‑care record.
| `GET` | `/api/aftercare/cliente/{clienteId}` | List all after‑care records for a given client.
| `PUT` | `/api/aftercare/{id}` | Update notes for an after‑care record.
| `DELETE` | `/api/aftercare/{id}` | Delete an after‑care record.

### Automatic Healing Progress
A daily scheduled job (`AftercareSchedulerService`) updates the `healingProgress` field based on the number of days elapsed since the procedure (30 days → 100 %).

### Front‑end
A new React page `src/pages/Aftercare.jsx` (not included in this backend repo) can consume these endpoints to display:
- Procedure date
- Days since procedure
- Healing percentage bar
- Notes and edit capabilities

### Future work
- Email reminders via `EmailService`
- WhatsApp integration (planned)

---

## New Feature: Budget (Orçamento)

- Added Flyway migrations V9 and V10 for `orcamentos`, `projetos`, and `kanban_cards` tables.
- Implemented backend entities, repositories, DTOs, services, and REST controllers for Orcamento, Projeto, and KanbanCard.
- Added React pages and components: `Orcamento.jsx`, `KanbanBoard.jsx`, and `ProjectApprovalModal.jsx`.
- Updated API documentation to include new endpoints.
