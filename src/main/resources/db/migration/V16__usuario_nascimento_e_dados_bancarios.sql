-- V16: data de nascimento (campanhas de aniversário) e dados bancários/Pix
-- de tatuadores (pagamento de comissões). Todos opcionais.

ALTER TABLE usuarios
    ADD COLUMN data_nascimento DATE NULL,
    ADD COLUMN pix_chave VARCHAR(140) NULL,
    ADD COLUMN banco VARCHAR(60) NULL,
    ADD COLUMN agencia VARCHAR(20) NULL,
    ADD COLUMN conta VARCHAR(30) NULL;
