-- =============================================================================
-- TattOS ERP - Migração V5: Pagamento de Sinal via PIX e Conciliação
-- Version: V5__adicionar_controle_pagamento_sinal_pix.sql
-- =============================================================================

ALTER TABLE agendamentos
    ADD COLUMN sinal_pago BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN codigo_pix_sinal VARCHAR(255);

CREATE INDEX idx_agendamentos_sinal ON agendamentos (sinal_pago, status);
