# ==================== TattOS - Makefile ====================================================================

# Antes de executar os comandos abaixo, certifique-se de que o "make" está instalado na sua máquina.

# Para verificar: make --version

# Depois, veja os comandos disponíveis: make help

# Para iniciar o projeto: make run
# ============================================================================================================

.PHONY: help run run-logs down stop restart logs logs-app logs-db db build test clean

help: ## Mostra os comandos disponíveis
	@echo ""
	@echo "TattOS - Comandos disponíveis:"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'
	@echo ""

run: ## Inicia o sistema e exibe os logs no console
	docker compose up -d --build
	docker compose logs -f app

run-logs: ## Alias para subir e acompanhar logs
	$(MAKE) run

down: ## Para o sistema
	docker compose down

stop: ## Alias para parar o sistema
	$(MAKE) down

restart: ## Reinicia o sistema
	docker compose restart

logs: ## Mostra os logs de todos os serviços
	docker compose logs -f

logs-app: ## Mostra os logs da aplicação backend
	docker compose logs -f app

logs-db: ## Mostra os logs do MySQL
	docker compose logs -f db

db: ## Inicia somente o MySQL
	docker compose up -d db

build: ## Compila o projeto localmente
	./gradlew build -x test

test: ## Executa os testes automatizados
	./gradlew test

clean: ## Limpa o projeto e os containers/volumes
	./gradlew clean
	docker compose down -v --remove-orphans
