-- V12__create_google_accounts_table.sql
-- Cria a tabela google_accounts que corresponde à entidade com.tattos.model.GoogleAccount
CREATE TABLE google_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL UNIQUE,
    google_user_id VARCHAR(255) NOT NULL UNIQUE,
    access_token VARCHAR(2048) NOT NULL,
    refresh_token VARCHAR(2048) NOT NULL,
    expiry_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_google_account_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
