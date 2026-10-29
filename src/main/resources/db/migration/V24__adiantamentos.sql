-- V24: adiantamentos a tatuadores viram entidade de primeira classe:
-- coluna de controle de abatimento (FIFO nos pagamentos de comissão) e recibo.
-- O tipo ADIANTAMENTO já existe no enum TipoTransacao; estas colunas só fazem
-- sentido para transações desse tipo.

ALTER TABLE transacoes_financeiras
    ADD COLUMN abatido DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    ADD COLUMN recibo_id BIGINT NULL,
    ADD CONSTRAINT fk_transacao_recibo FOREIGN KEY (recibo_id) REFERENCES arquivos (id);

CREATE INDEX idx_transacao_adiantamento_aberto
    ON transacoes_financeiras (tatuador_id, tipo, abatido);
