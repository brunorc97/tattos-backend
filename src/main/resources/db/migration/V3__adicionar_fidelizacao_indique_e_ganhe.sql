-- =============================================================================
-- TattOS ERP - Migração V3: Fidelização (Indique e Ganhe) — backfill de dados
-- O DDL (colunas codigo_indicacao e desconto_aplicado) vive em V6.
-- =============================================================================

-- Gerar códigos de indicação apenas para CLIENTES já existentes sem código.
-- Tatuadores/gestores não usam código de indicação.
UPDATE usuarios
SET codigo_indicacao = CONCAT('TATTO', LPAD(id, 6, '0'))
WHERE codigo_indicacao IS NULL
  AND role = 'ROLE_CLIENTE';
