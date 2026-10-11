-- =============================================================================
-- TattOS ERP - Migração V3: Fidelização (Indique e Ganhe)
-- Version: V3__adicionar_fidelizacao_indique_e_ganhe.sql
-- =============================================================================

-- 1. Adicionar código de indicação único para usuários
ALTER TABLE usuarios
    ADD COLUMN codigo_indicacao VARCHAR(20) UNIQUE;

-- 2. Gerar códigos de indicação para usuários já existentes
UPDATE usuarios
SET codigo_indicacao = CONCAT('TATTO', LPAD(id, 6, '0'))
WHERE codigo_indicacao IS NULL;

-- 3. Adicionar coluna de desconto aplicado na tabela de agendamentos
ALTER TABLE agendamentos
    ADD COLUMN desconto_aplicado DECIMAL(10, 2) NOT NULL DEFAULT 0.00;
