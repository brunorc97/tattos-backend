-- V7__adicionar_tabela_assinatura_log.sql
CREATE TABLE assinatura_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    ficha_anamnese_id BIGINT NOT NULL,
    data_assinatura TIMESTAMP NOT NULL,
    email VARCHAR(255) NOT NULL,
    usuario_id BIGINT,
    CONSTRAINT fk_assinatura_ficha FOREIGN KEY (ficha_anamnese_id) REFERENCES fichas_anamnese(id)
    -- opcional: FK para usuario que assinou, se necessário
);
