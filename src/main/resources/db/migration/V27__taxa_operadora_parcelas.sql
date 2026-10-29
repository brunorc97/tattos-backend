-- V27: taxas do crédito parcelado por número de parcelas (1x–10x) por operadora.
-- A modalidade CREDITO_PARCELADO passa a exigir a grade; débito/crédito à vista
-- seguem com taxa única em taxa_percentual.

CREATE TABLE taxas_operadoras_parcelas (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    taxa_operadora_id BIGINT       NOT NULL,
    parcelas          INT          NOT NULL,
    taxa_percentual   DECIMAL(5,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_taxa_parcela UNIQUE (taxa_operadora_id, parcelas),
    CONSTRAINT fk_taxa_parcela_operadora FOREIGN KEY (taxa_operadora_id)
        REFERENCES taxas_operadoras (id) ON DELETE CASCADE,
    CONSTRAINT chk_parcelas_range CHECK (parcelas BETWEEN 1 AND 10)
);
