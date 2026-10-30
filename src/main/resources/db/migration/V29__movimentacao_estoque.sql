CREATE TABLE movimentacao_estoque (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insumo_id BIGINT NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    quantidade INT NOT NULL,
    custo_unitario DECIMAL(10,2) NULL,
    retirado_por_id BIGINT NULL,
    motivo VARCHAR(255) NULL,
    created_by VARCHAR(150) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mov_insumo FOREIGN KEY (insumo_id) REFERENCES insumos(id),
    CONSTRAINT fk_mov_retirado_por FOREIGN KEY (retirado_por_id) REFERENCES usuarios(id)
);
