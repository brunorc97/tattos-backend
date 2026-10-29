-- =============================================================================
-- TattOS ERP - Migração V6: Código de indicação (usuários) e desconto (agendamentos)
-- codigo_indicacao: exclusivo de clientes (gerado pelo backend para novos clientes).
-- =============================================================================

ALTER TABLE usuarios
    ADD COLUMN codigo_indicacao VARCHAR(20) UNIQUE;

ALTER TABLE agendamentos
    ADD COLUMN desconto_aplicado DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
