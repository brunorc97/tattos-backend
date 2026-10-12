-- =============================================================================
-- TattOS ERP - Migração V8: Campos de pagamento adicional e status de pagamento completo
-- Version: V8__adicionar_campos_pagamento_adicional.sql
-- =============================================================================

ALTER TABLE agendamentos
    ADD COLUMN valor_cobranca_adicional NUMERIC(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN pagamento_completo BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_pagamento TIMESTAMP;

CREATE INDEX idx_agendamentos_pagamento ON agendamentos (pagamento_completo, sinal_pago);
