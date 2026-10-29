-- V19: Projeto vinculado a cliente/tatuador, com prazo, decisão de aprovação
-- (com motivo de recusa) e controle de sinal com comprovante.
-- O boolean legado `aprovado` é convertido para o enum status_aprovacao.

ALTER TABLE projetos
    ADD COLUMN cliente_id BIGINT NULL,
    ADD COLUMN tatuador_id BIGINT NULL,
    ADD COLUMN prazo DATE NULL,
    ADD COLUMN status_aprovacao VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    ADD COLUMN motivo_recusa VARCHAR(30) NULL,
    ADD COLUMN motivo_recusa_detalhe VARCHAR(500) NULL,
    ADD COLUMN necessita_sinal BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN valor_sinal DECIMAL(10,2) NULL,
    ADD COLUMN sinal_pago BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN data_pagamento_sinal TIMESTAMP NULL,
    ADD COLUMN forma_pagamento_sinal VARCHAR(30) NULL,
    ADD COLUMN comprovante_sinal_id BIGINT NULL;

UPDATE projetos SET status_aprovacao = 'APROVADO' WHERE aprovado = TRUE;

ALTER TABLE projetos DROP COLUMN aprovado;

ALTER TABLE projetos
    ADD CONSTRAINT fk_projeto_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_projeto_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id),
    ADD CONSTRAINT fk_projeto_comprovante FOREIGN KEY (comprovante_sinal_id) REFERENCES arquivos (id);

CREATE INDEX idx_projetos_status ON projetos (status_aprovacao);
CREATE INDEX idx_projetos_tatuador ON projetos (tatuador_id);
CREATE INDEX idx_projetos_cliente ON projetos (cliente_id);
