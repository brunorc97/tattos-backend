ALTER TABLE orcamentos
    ADD COLUMN valor_total DECIMAL(10,2) NULL,
    ADD COLUMN valor_sessao DECIMAL(10,2) NULL,
    ADD COLUMN sessoes_min INT NULL,
    ADD COLUMN sessoes_max INT NULL,
    ADD COLUMN tipo_cobranca VARCHAR(20) NULL,
    ADD COLUMN observacoes VARCHAR(500) NULL,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO';
