-- =============================================================================
-- TattOS ERP - Migração V3: Fidelização (Indique e Ganhe) — DDL
-- codigo_indicacao: exclusivo de clientes (gerado pelo backend para novos clientes).
-- O backfill para bases legadas acontece na V6 (após o seed da V2).
-- =============================================================================

ALTER TABLE usuarios
    ADD COLUMN codigo_indicacao VARCHAR(20) UNIQUE;

ALTER TABLE agendamentos
    ADD COLUMN desconto_aplicado DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
