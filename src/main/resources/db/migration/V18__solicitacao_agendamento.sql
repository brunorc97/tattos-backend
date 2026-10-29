-- V18: solicitações de agendamento iniciadas pelo cliente (ideia, referências,
-- preferências), aprovadas/recusadas pelo estúdio antes de virar agendamento.

CREATE TABLE solicitacoes_agendamento (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    cliente_id            BIGINT       NOT NULL,
    tatuador_id           BIGINT       NULL,
    projeto_id            BIGINT       NULL,
    imagem_referencia_id  BIGINT       NULL,
    ideia                 TEXT         NOT NULL,
    estilo                VARCHAR(100) NULL,
    dia_preferencia       DATE         NULL,
    semana_preferencia    VARCHAR(10)  NULL,
    status                VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE',
    motivo_recusa         VARCHAR(500) NULL,
    agendamento_id        BIGINT       NULL,
    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_solic_cliente   FOREIGN KEY (cliente_id)   REFERENCES usuarios (id),
    CONSTRAINT fk_solic_tatuador  FOREIGN KEY (tatuador_id)  REFERENCES usuarios (id),
    CONSTRAINT fk_solic_projeto   FOREIGN KEY (projeto_id)   REFERENCES projetos (id),
    CONSTRAINT fk_solic_imagem    FOREIGN KEY (imagem_referencia_id) REFERENCES arquivos (id),
    CONSTRAINT fk_solic_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id),
    INDEX idx_solic_status (status),
    INDEX idx_solic_cliente (cliente_id),
    INDEX idx_solic_tatuador (tatuador_id)
);
