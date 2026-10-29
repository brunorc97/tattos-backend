-- =============================================================================
-- TattOS ERP - Migração V3: Fidelização (Indique e Ganhe)
-- DDL idempotente: as colunas já são criadas na V2 para bancos novos; aqui
-- ficam os guards para bancos criados por versões antigas da V2.
-- =============================================================================

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuarios' AND COLUMN_NAME = 'codigo_indicacao'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE usuarios ADD COLUMN codigo_indicacao VARCHAR(20) UNIQUE',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'agendamentos' AND COLUMN_NAME = 'desconto_aplicado'
);
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE agendamentos ADD COLUMN desconto_aplicado DECIMAL(10, 2) NOT NULL DEFAULT 0.00',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Backfill: código de indicação apenas para CLIENTES sem código.
UPDATE usuarios
SET codigo_indicacao = CONCAT('TATTO', LPAD(id, 6, '0'))
WHERE codigo_indicacao IS NULL
  AND role = 'ROLE_CLIENTE';
