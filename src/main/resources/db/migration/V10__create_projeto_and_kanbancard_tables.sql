-- V10__create_projeto_and_kanbancard_tables.sql
-- Create tables for Projeto (Project) and KanbanCard (Kanban board cards)

CREATE TABLE projetos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    aprovado BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE kanban_cards (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    status VARCHAR(50) NOT NULL,
    ordem INT NOT NULL,
    orcamento_id BIGINT,
    projeto_id BIGINT,
    CONSTRAINT fk_kanban_orcamento FOREIGN KEY (orcamento_id) REFERENCES orcamentos(id),
    CONSTRAINT fk_kanban_projeto FOREIGN KEY (projeto_id) REFERENCES projetos(id)
);
