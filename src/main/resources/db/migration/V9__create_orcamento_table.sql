-- V9__create_orcamento_table.sql
-- Create table for Orcamento (Budget)
CREATE TABLE orcamentos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255) NOT NULL,
    valor NUMERIC(10,2) NOT NULL,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    projeto_id BIGINT
-- Removed FK to projetos; will be added in later migration
);
