CREATE TABLE studio_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NULL,
    descricao VARCHAR(1000) NULL,
    endereco VARCHAR(255) NULL,
    horario VARCHAR(150) NULL,
    instagram VARCHAR(150) NULL
);

CREATE TABLE trabalho (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tatuador_id BIGINT NOT NULL,
    cliente_id BIGINT NULL,
    trabalho_realizado_id BIGINT NULL,
    titulo VARCHAR(150) NOT NULL,
    estilo VARCHAR(100) NULL,
    descricao VARCHAR(1000) NULL,
    foto_url VARCHAR(500) NULL,
    data DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trabalho_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios(id),
    CONSTRAINT fk_trabalho_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_trabalho_realizado FOREIGN KEY (trabalho_realizado_id) REFERENCES trabalhos_realizados(id)
);

CREATE TABLE ideia_tatuagem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    estilo VARCHAR(100) NULL,
    descricao VARCHAR(1000) NULL,
    imagem_url VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE avaliacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trabalho_id BIGINT NOT NULL UNIQUE,
    cliente_id BIGINT NOT NULL,
    tatuador_id BIGINT NOT NULL,
    nota_tatuagem INT NOT NULL,
    nota_tatuador INT NOT NULL,
    comentario VARCHAR(1000) NULL,
    data DATE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_avaliacao_trabalho FOREIGN KEY (trabalho_id) REFERENCES trabalho(id),
    CONSTRAINT fk_avaliacao_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_avaliacao_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios(id)
);

ALTER TABLE usuarios ADD COLUMN foto_url VARCHAR(500) NULL;

INSERT INTO studio_info (nome, descricao, endereco, horario, instagram)
VALUES ('TattOS Studio', 'Estúdio de tatuagem e body art.', NULL, 'Seg–Sáb, 09h às 19h', NULL);
