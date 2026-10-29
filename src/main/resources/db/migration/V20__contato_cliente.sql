-- V20: histórico de contato do cliente (CRM) — ligações, WhatsApp, e-mail, presencial.

CREATE TABLE contatos_cliente (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    cliente_id    BIGINT       NOT NULL,
    autor_id      BIGINT       NULL,
    tipo          VARCHAR(20)  NOT NULL,
    resumo        VARCHAR(1000) NOT NULL,
    data_contato  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_contato_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id),
    CONSTRAINT fk_contato_autor   FOREIGN KEY (autor_id)   REFERENCES usuarios (id),
    INDEX idx_contato_cliente (cliente_id, data_contato)
);
