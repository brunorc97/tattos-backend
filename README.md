# TattOS: Sistema de Gestão Integrada para Estúdios de Tatuagem e Body Art

![Java 25](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.8-brightgreen?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-blue?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?logo=react&logoColor=black)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?logo=tailwind-css&logoColor=white)

O **TattOS** é um ERP (Sistema de Gestão Integrada) desenvolvido em arquitetura distribuída, projetado especificamente para atuar como o sistema operacional central de estúdios de tatuagem e artistas independentes de *body art*. O sistema unifica a gestão operacional (agendamentos e biossegurança) e a administração financeira/estoque em uma única plataforma de alta performance.

---

## 🚀 Tech Stack

### Back-end (API RESTful)
* **Java 25 (LTS)** — Linguagem principal de altíssimo desempenho.
* **Spring Boot 4.0.8** — Framework robusto para a construção da API REST.
* **Spring Security + JWT** — Autenticação stateless e controle de acesso baseado em tokens criptografados.
* **Spring Data JPA & Hibernate** — Mapeamento objeto-relacional (ORM) e persistência segura.
* **Spring Validation** — Validação rigorosa de regras de negócio no servidor.
* **MySQL 8.4** — Banco de dados relacional para armazenamento estruturado.
* **Gradle** — Gerenciador de dependências e automação de build.
* **Docker & Docker Compose** — Containerização completa da aplicação e banco de dados.

### Front-end (Client-side)
* **React** — Biblioteca JavaScript para interfaces componentizadas e reativas.
* **Tailwind CSS** — Estilização orientada a utilitários com suporte ao Design System **Dark Cyber-Chicano**.

---

## ⚙️ Principais Funcionalidades (Features)

1. **Gestão de Agenda e Agendamento Inteligente:** Prevenção de conflitos de horários entre artistas, clientes e recursos do estúdio.
2. **Ficha de Anamnese Digital (Biossegurança):** Termos de saúde e responsabilidade preenchidos digitalmente com assinatura em alta precisão.
3. **Módulo Financeiro ("Smile Now, Cry Later"):** Dashboard gerencial confrontando o faturamento bruto com custos fixos e despesas de insumos.
4. **Controle de Estoque Crítico:** Monitoramento em tempo real de agulhas, tintas e biqueiras inspirado em medidores de suspensão hidráulica.

---

## 📂 Estrutura do Back-end

O projeto segue princípios de Clean Architecture / Domain-Driven Design (DDD) modularizado:

```text
src/main/java/com/tattos/
├── config/             # Configurações do Spring (Security, CORS, WebMvc, etc.)
├── controller/         # Controladores REST expondo as rotas da API
├── dto/
│   ├── request/        # Data Transfer Objects para entrada de dados
│   └── response/       # Data Transfer Objects para saída de dados
├── exception/          # Tratamento centralizado de exceções (GlobalExceptionHandler)
├── model/              # Entidades JPA de domínio do negócio
├── repository/         # Interfaces de acesso a dados (Spring Data JPA)
├── security/           # Componentes JWT, filtros e utilitários de segurança
├── service/            # Regras de negócio e casos de uso
└── TattosApplication.java # Ponto de entrada da aplicação Spring Boot
```

---

## 🛠️ Como Executar o Projeto Localmente

### Pré-requisitos
* **Java Development Kit (JDK) 25** instalado.
* **Docker** e **Docker Compose** instalados (opcional, mas recomendado).
* **MySQL** (caso execute fora do Docker).
* **Node.js (v18+)** e **npm/yarn/pnpm** (para o front-end).

---

### Opção 1: Via Docker & Makefile (Recomendado)

O projeto conta com automação via `Makefile` e `docker-compose.yml` para facilitar o fluxo de desenvolvimento:

```bash
# Iniciar banco MySQL + aplicação Spring Boot em background
make run

# Acompanhar logs em tempo real
make logs

# Acompanhar logs apenas da aplicação backend
make logs-app

# Parar os containers
make down

# Limpar builds e volumes
make clean
```

> A aplicação estará acessível em: `http://localhost:8080`

---

### Opção 2: Execução Manual com Gradle

#### 1. Iniciar apenas o banco de dados MySQL via Docker:
```bash
make db
```
*(Ou utilize uma instância local do MySQL na porta `3306` com a base `tattos_db`)*

#### 2. Configurar as variáveis de ambiente (opcional, valores padrão disponíveis):
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=tattos_db
export DB_USERNAME=root
export DB_PASSWORD=root
export JWT_SECRET=minha-chave-secreta-super-segura-para-jwt-tattos
```

#### 3. Compilar e executar a aplicação:
```bash
# Build e execução com Gradle Wrapper
./gradlew bootRun
```

#### 4. Executar os testes automatizados:
```bash
./gradlew test
# ou via Makefile
make test
```

---

## 📋 Comandos Disponíveis no Makefile

Execute `make help` para visualizar todos os comandos:

| Comando | Descrição |
|---|---|
| `make run` | Constrói e inicia todos os containers em background (App + MySQL) |
| `make run-logs` | Constrói e inicia todos os containers com logs no terminal |
| `make down` | Para e remove todos os containers e redes |
| `make restart` | Reinicia os containers da aplicação |
| `make logs` | Exibe os logs de todos os containers em tempo real |
| `make logs-app` | Exibe os logs específicos do backend |
| `make logs-db` | Exibe os logs do MySQL |
| `make db` | Inicia apenas o container do banco de dados MySQL |
| `make build` | Compila o projeto localmente com Gradle (sem rodar testes) |
| `make test` | Executa a suíte de testes automatizados |
| `make clean` | Limpa build local e volumes/containers órfãos |

---

## 🔒 Autenticação e Segurança

* Rotas públicas: `/api/auth/**` (ex: login, registro)
* Demais rotas: Protegidas via Bearer Token JWT no header HTTP:
  ```http
  Authorization: Bearer <seu_token_jwt>
  ```

---

## 📄 Licença

Este projeto está sob a licença [MIT](LICENSE).
