.PHONY: help run run-logs down stop restart logs logs-app logs-db db build test clean

help: ## Exibe a lista de comandos disponíveis
	@echo "Comandos disponíveis no Makefile:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

run: ## Constrói e inicia todos os containers em background (App + MySQL)
	docker compose up -d --build

run-logs: ## Constrói e inicia todos os containers exibindo os logs em tempo real
	docker compose up --build

down: ## Para e remove todos os containers e redes
	docker compose down

stop: down ## Alias para down

restart: ## Reinicia os containers da aplicação
	docker compose restart

logs: ## Exibe os logs de todos os containers em tempo real
	docker compose logs -f

logs-app: ## Exibe os logs apenas do backend
	docker compose logs -f app

logs-db: ## Exibe os logs apenas do banco MySQL
	docker compose logs -f db

db: ## Inicia apenas o container do banco de dados MySQL
	docker compose up -d db

build: ## Compila o projeto localmente com o Gradle
	./gradlew build -x test

test: ## Executa a suíte de testes automatizados
	./gradlew test

clean: ## Limpa o build local e volumes/containers órfãos do Docker
	./gradlew clean
	docker compose down -v --remove-orphans
