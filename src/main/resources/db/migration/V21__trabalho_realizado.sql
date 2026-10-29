-- V21: trabalhos realizados no cliente (galeria do CRM) — foto, tatuador, data.

CREATE TABLE trabalhos_realizados (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    cliente_id     BIGINT        NOT NULL,
    tatuador_id    BIGINT        NOT NULL,
    agendamento_id BIGINT        NULL,
    imagem_id      BIGINT        NULL,
    descricao      VARCHAR(500)  NULL,
    data_trabalho  DATE          NOT NULL,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_trabalho_cliente   FOREIGN KEY (cliente_id)     REFERENCES usuarios (id),
    CONSTRAINT fk_trabalho_tatuador  FOREIGN KEY (tatuador_id)    REFERENCES usuarios (id),
    CONSTRAINT fk_trabalho_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id),
    CONSTRAINT fk_trabalho_imagem    FOREIGN KEY (imagem_id)      REFERENCES arquivos (id),
    INDEX idx_trabalho_cliente (cliente_id, data_trabalho)
);
