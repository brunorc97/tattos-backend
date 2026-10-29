-- V22: taxas das maquininhas/operadoras de cartão por modalidade
-- (débito, crédito à vista, crédito parcelado). Usadas para calcular o valor
-- líquido das receitas pagas em cartão.

CREATE TABLE taxas_operadoras (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    operadora        VARCHAR(60)  NOT NULL,
    modalidade       VARCHAR(30)  NOT NULL,
    taxa_percentual  DECIMAL(5,2) NOT NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_operadora_modalidade UNIQUE (operadora, modalidade)
);
