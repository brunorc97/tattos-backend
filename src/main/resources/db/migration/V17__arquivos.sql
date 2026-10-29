-- V17: metadados de arquivos enviados (comprovantes, recibos, fotos de trabalhos).
-- O binário fica em disco (app.storage.upload-dir); aqui só os metadados.

CREATE TABLE arquivos (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nome_original   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(100) NOT NULL,
    tamanho_bytes   BIGINT       NOT NULL,
    caminho         VARCHAR(500) NOT NULL,
    dono_id         BIGINT       NULL,
    created_by      VARCHAR(150),
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_arquivo_dono FOREIGN KEY (dono_id) REFERENCES usuarios (id) ON DELETE SET NULL
);
