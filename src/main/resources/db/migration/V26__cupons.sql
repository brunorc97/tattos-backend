-- V26: cupons de desconto (promoções), distintos do código de indicação.
-- Abrangência: tatuador_id NULL = todo o studio; preenchido = só aquele tatuador.

CREATE TABLE cupons (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    codigo         VARCHAR(40)   NOT NULL,
    tipo           VARCHAR(20)   NOT NULL, -- PORCENTAGEM | VALOR_FIXO
    valor          DECIMAL(10,2) NOT NULL,
    tatuador_id    BIGINT        NULL,
    validade       DATE          NULL,
    limite_usos    INT           NULL,
    usos           INT           NOT NULL DEFAULT 0,
    ativo          BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_por     BIGINT        NULL,
    created_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_cupom_codigo UNIQUE (codigo),
    CONSTRAINT fk_cupom_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id),
    CONSTRAINT fk_cupom_autor    FOREIGN KEY (criado_por)  REFERENCES usuarios (id)
);

ALTER TABLE agendamentos
    ADD COLUMN cupom_id BIGINT NULL,
    ADD CONSTRAINT fk_agendamento_cupom FOREIGN KEY (cupom_id) REFERENCES cupons (id);
