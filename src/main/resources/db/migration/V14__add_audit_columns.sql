-- V14__add_audit_columns.sql
-- Adiciona colunas de auditoria (created_by, last_modified_by, updated_at) para as entidades principais

ALTER TABLE usuarios 
    ADD COLUMN created_by VARCHAR(150) NULL DEFAULT 'SISTEMA',
    ADD COLUMN last_modified_by VARCHAR(150) NULL DEFAULT 'SISTEMA';

ALTER TABLE agendamentos 
    ADD COLUMN created_by VARCHAR(150) NULL DEFAULT 'SISTEMA',
    ADD COLUMN last_modified_by VARCHAR(150) NULL DEFAULT 'SISTEMA';

ALTER TABLE insumos 
    ADD COLUMN created_by VARCHAR(150) NULL DEFAULT 'SISTEMA',
    ADD COLUMN last_modified_by VARCHAR(150) NULL DEFAULT 'SISTEMA';

ALTER TABLE transacoes_financeiras 
    ADD COLUMN created_by VARCHAR(150) NULL DEFAULT 'SISTEMA',
    ADD COLUMN last_modified_by VARCHAR(150) NULL DEFAULT 'SISTEMA';

ALTER TABLE fichas_anamnese 
    ADD COLUMN created_by VARCHAR(150) NULL DEFAULT 'SISTEMA',
    ADD COLUMN last_modified_by VARCHAR(150) NULL DEFAULT 'SISTEMA',
    ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
