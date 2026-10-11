-- =============================================================================
-- TattOS ERP - Migração V4: Controle de Lembretes de Agendamentos
-- Version: V4__adicionar_controle_lembrete_agendamentos.sql
-- =============================================================================

-- Adicionar flag de idempotência para envio de lembretes automáticos
ALTER TABLE agendamentos
    ADD COLUMN lembrete_enviado BOOLEAN NOT NULL DEFAULT FALSE;

-- Índice para acelerar a busca de agendamentos pendentes de notificação
CREATE INDEX idx_agendamentos_lembrete ON agendamentos (lembrete_enviado, data_hora, status);
