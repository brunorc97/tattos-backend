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
Agendamentos	Controle de sessões e horários	✅
Anamnese	Fichas de anamnese dos clientes	✅
Estoque	Controle de insumos utilizados pelo estúdio	✅
Financeiro	Registro de receitas e despesas	✅
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
            │   ├── EstoqueController
            │   ├── FinanceiroController
            │   ├── FichaAnamneseController
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
            │   ├── Usuario
            │   ├── Agendamento
            │   ├── Insumo
            │   ├── TransacaoFinanceira
            │   └── FichaAnamnese
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
Front-end	React 18 — planejado


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
Anamnese	/api/fichas-anamnese/**	✅	✅	✅
Estoque	/api/estoque/**	✅	✅	❌
Financeiro	/api/financeiro/**	✅	✅	❌
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

* Usuario
* Agendamento
* FichaAnamnese
* Insumo
* TransacaoFinanceira

⸻

Evolução do projeto

O desenvolvimento foi organizado em sprints, permitindo a implementação incremental das funcionalidades.

Sprint	Objetivo	Status
Sprint 0	Setup do projeto, Gradle, Docker e estrutura inicial	✅
Sprint 1	Modelagem das entidades, banco e migrations	✅
Sprint 2	Controllers, DTOs, Services e tratamento de exceções	✅
Sprint 3	Autenticação JWT, Spring Security e testes	✅
Sprint 4	OpenAPI, segurança avançada, logs e configuração de produção	🔄

⸻

Como executar

Pré-requisitos

Para executar o projeto utilizando Docker:

* Docker 24+
* Docker Compose 2.20+
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

Variável	Padrão de desenvolvimento	Descrição
DB_HOST	db	Host do MySQL
DB_PORT	3306	Porta do MySQL
DB_NAME	tattos_db	Nome do banco
DB_USERNAME	root	Usuário do banco
DB_PASSWORD	root	Senha do banco
JWT_SECRET	Valor de desenvolvimento	Chave utilizada para assinatura do JWT
SPRING_PROFILES_ACTIVE	dev	Profile da aplicação
CORS_ALLOWED_ORIGINS	http://localhost:3000	Origens autorizadas

 Importante: valores sensíveis utilizados em produção não devem ser versionados no Git.

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

* Desenvolvimento do front-end em React;
* Dashboard gerencial;
* Relatórios financeiros;
* Alertas de estoque mínimo;
* Interface responsiva;
* Sistema de notificações;
* Integração com meios de pagamento;
* Monitoramento com Prometheus e Grafana;
* Deploy em ambiente cloud;
* Orquestração com Kubernetes;
* Suporte multiestúdio / multi-tenant.

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
