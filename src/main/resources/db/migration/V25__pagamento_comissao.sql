-- V25: pagamentos de comissões a tatuadores (seleção de trabalhos → agendado → pago),
-- com abatimento de adiantamentos e recibo em PDF persistido.

ALTER TABLE transacoes_financeiras
    ADD COLUMN status_repasse VARCHAR(20) NOT NULL DEFAULT 'PENDENTE';

CREATE TABLE pagamentos_comissao (
    id                          BIGINT        NOT NULL AUTO_INCREMENT,
    tatuador_id                 BIGINT        NOT NULL,
    total_bruto                 DECIMAL(10,2) NOT NULL,
    total_adiantamentos_abatidos DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_liquido               DECIMAL(10,2) NOT NULL,
    forma_pagamento             VARCHAR(20)   NOT NULL,
    status                      VARCHAR(20)   NOT NULL DEFAULT 'AGENDADO',
    comprovante_id              BIGINT        NULL,
    recibo_arquivo_id           BIGINT        NULL,
    created_by                  VARCHAR(150),
    created_at                  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    pago_em                     TIMESTAMP     NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_pagcom_tatuador    FOREIGN KEY (tatuador_id)         REFERENCES usuarios (id),
    CONSTRAINT fk_pagcom_comprovante FOREIGN KEY (comprovante_id)      REFERENCES arquivos (id),
    CONSTRAINT fk_pagcom_recibo      FOREIGN KEY (recibo_arquivo_id)   REFERENCES arquivos (id),
    INDEX idx_pagcom_tatuador (tatuador_id, status)
);

CREATE TABLE pagamentos_comissao_itens (
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    pagamento_id       BIGINT NOT NULL,
    transacao_id       BIGINT NOT NULL,
    valor_repasse      DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_pagcom_item_transacao UNIQUE (transacao_id),
    CONSTRAINT fk_pagcom_item_pagamento FOREIGN KEY (pagamento_id) REFERENCES pagamentos_comissao (id),
    CONSTRAINT fk_pagcom_item_transacao FOREIGN KEY (transacao_id) REFERENCES transacoes_financeiras (id)
);

CREATE INDEX idx_transacao_status_repasse ON transacoes_financeiras (tatuador_id, status_repasse);
