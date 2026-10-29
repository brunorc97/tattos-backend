-- V23: vínculo da transação com a taxa da operadora e valores calculados
-- (persistidos na criação — edição de taxa não recalcula o passado).

ALTER TABLE transacoes_financeiras
    ADD COLUMN taxa_operadora_id BIGINT NULL,
    ADD COLUMN taxa_valor DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN valor_liquido DECIMAL(10,2) NULL,
    ADD CONSTRAINT fk_transacao_taxa FOREIGN KEY (taxa_operadora_id) REFERENCES taxas_operadoras (id);

-- backfill: valor_liquido = valor - 0 para o histórico sem taxa
UPDATE transacoes_financeiras SET valor_liquido = valor WHERE valor_liquido IS NULL;
