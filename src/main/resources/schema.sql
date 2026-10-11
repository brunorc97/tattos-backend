-- =============================================================================
-- TattOS ERP - Database Schema (MySQL 8.4 / H2 Compatible)
-- File: schema.sql
-- =============================================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    telefone VARCHAR(30),
    endereco VARCHAR(255),
    especialidade VARCHAR(100),
    bio TEXT,
    cargo VARCHAR(100),
    comissao_percentual DECIMAL(5, 2) DEFAULT 70.00,
    codigo_indicacao VARCHAR(20) UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora DATETIME NOT NULL,
    duracao_minutos INT NOT NULL DEFAULT 60,
    data_hora_fim DATETIME NOT NULL,
    cabine VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AGENDADO',
    valor DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    valor_sinal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    desconto_aplicado DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    lembrete_enviado BOOLEAN NOT NULL DEFAULT FALSE,
    sinal_pago BOOLEAN NOT NULL DEFAULT FALSE,
    codigo_pix_sinal VARCHAR(255),
    observacoes TEXT,
    cliente_id BIGINT NOT NULL,
    tatuador_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_agendamento_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS fichas_anamnese (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    agendamento_id BIGINT NULL,
    historico_saude TEXT,
    alergias TEXT,
    termo_assinado BOOLEAN NOT NULL DEFAULT FALSE,
    assinatura_digital LONGTEXT,
    alerta_clinico BOOLEAN NOT NULL DEFAULT FALSE,
    risco_dermatologico BOOLEAN NOT NULL DEFAULT FALSE,
    diabetes BOOLEAN NOT NULL DEFAULT FALSE,
    hemofilia BOOLEAN NOT NULL DEFAULT FALSE,
    hipertensao BOOLEAN NOT NULL DEFAULT FALSE,
    gestante_lactante BOOLEAN NOT NULL DEFAULT FALSE,
    medicamentos_uso TEXT,
    observacoes TEXT,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_anamnese_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_anamnese_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) UNIQUE,
    nome VARCHAR(150) NOT NULL,
    quantidade_atual INT NOT NULL DEFAULT 0,
    nivel_critico INT NOT NULL DEFAULT 10,
    unidade_medida VARCHAR(30) NOT NULL DEFAULT 'UN',
    categoria VARCHAR(80),
    lote VARCHAR(50),
    data_validade DATE,
    valor_unitario DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transacoes_financeiras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    data DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONCLUIDO',
    tatuador_id BIGINT NULL,
    categoria VARCHAR(80),
    forma_pagamento VARCHAR(50),
    comissao_percentual DECIMAL(5, 2) DEFAULT 0.00,
    valor_repasse DECIMAL(10, 2) DEFAULT 0.00,
    agendamento_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacao_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id) ON DELETE SET NULL,
    CONSTRAINT fk_transacao_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE SET NULL
);
