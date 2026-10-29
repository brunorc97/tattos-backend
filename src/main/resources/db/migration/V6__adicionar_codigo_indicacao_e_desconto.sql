-- =============================================================================
-- TattOS ERP - Migração V6: backfill de código de indicação para bases legadas.
-- O DDL vive na V3 (com guards) e na V2 (bancos novos, antes do seed).
-- Gera código apenas para CLIENTES sem código; tatuadores/gestores ficam NULL.
-- Idempotente: sem efeito em bancos já migrados.
-- =============================================================================

UPDATE usuarios
SET codigo_indicacao = CONCAT('TATTO', LPAD(id, 6, '0'))
WHERE codigo_indicacao IS NULL
  AND role = 'ROLE_CLIENTE';
