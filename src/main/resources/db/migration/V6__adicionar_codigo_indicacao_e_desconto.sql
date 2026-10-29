-- =============================================================================
-- TattOS ERP - Migração V6: backfill de código de indicação para bases legadas.
-- O DDL (colunas codigo_indicacao e desconto_aplicado) vive na V3.
-- Gera código apenas para CLIENTES sem código; tatuadores/gestores ficam NULL.
-- =============================================================================

UPDATE usuarios
SET codigo_indicacao = CONCAT('TATTO', LPAD(id, 6, '0'))
WHERE codigo_indicacao IS NULL
  AND role = 'ROLE_CLIENTE';
