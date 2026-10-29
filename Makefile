# ==================== TattOS - Makefile ====================================================================

# Antes de executar os comandos abaixo, certifique-se de que o "make" está instalado na sua máquina.

# Para verificar: make --version

# Depois, veja os comandos disponíveis: make help

# Para iniciar o projeto: make run

# Docker agnóstico de plataforma (Linux/macOS): basta ter o Docker Desktop (macOS) ou o
# Docker Engine (Linux) instalado, junto com o Docker Compose v2 (plugin "docker compose").
# ============================================================================================================

# Detecta automaticamente "docker compose" (v2, plugin) ou "docker-compose" (v1, standalone)
DOCKER_COMPOSE := $(shell docker compose version >/dev/null 2>&1 && echo "docker compose" || echo "docker-compose")

.PHONY: help run run-logs down stop restart logs logs-app logs-db db build test clean

help: ## Mostra os comandos disponíveis
	@echo ""
	@echo "TattOS - Comandos disponíveis:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'
	@echo ""

run: ## Inicia o sistema e exibe os logs no console
	$(DOCKER_COMPOSE) up -d --build
	$(DOCKER_COMPOSE) logs -f app

run-logs: ## Alias para subir e acompanhar logs
	$(MAKE) run

down: ## Para o sistema
	$(DOCKER_COMPOSE) down

stop: ## Alias para parar o sistema
	$(MAKE) down

restart: ## Reinicia o sistema
	$(DOCKER_COMPOSE) restart

logs: ## Mostra os logs de todos os serviços
	$(DOCKER_COMPOSE) logs -f

logs-app: ## Mostra os logs da aplicação backend
	$(DOCKER_COMPOSE) logs -f app

logs-db: ## Mostra os logs do MySQL
	$(DOCKER_COMPOSE) logs -f db

db: ## Inicia somente o MySQL
	$(DOCKER_COMPOSE) up -d db

build: ## Compila o projeto localmente
	./gradlew build -x test

test: ## Executa os testes automatizados
	./gradlew test

clean: ## Limpa o projeto e os containers/volumes
	./gradlew clean
	$(DOCKER_COMPOSE) down -v --remove-orphans
