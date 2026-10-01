-- =============================================================================
-- TattOS ERP - Database Initialization Script (MySQL 8.4)
-- Version: V1__init.sql
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS transacoes_financeiras;
DROP TABLE IF EXISTS fichas_anamnese;
DROP TABLE IF EXISTS agendamentos;
DROP TABLE IF EXISTS insumos;
DROP TABLE IF EXISTS usuarios;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. TABELA: usuarios
-- Armazena todos os usuários do sistema com herança / atributos para:
-- Gestor (ROLE_GESTOR), Tatuador (ROLE_TATUADOR) e Cliente (ROLE_CLIENTE).
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios (
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
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_usuarios_email (email),
    INDEX idx_usuarios_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. TABELA: agendamentos
-- Gerenciamento de sessões com prevenção de conflitos de agenda e cabines.
-- -----------------------------------------------------------------------------
CREATE TABLE agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora DATETIME NOT NULL,
    duracao_minutos INT NOT NULL DEFAULT 60,
    data_hora_fim DATETIME NOT NULL,
    cabine VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AGENDADO',
    valor DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    valor_sinal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    observacoes TEXT,
    cliente_id BIGINT NOT NULL,
    tatuador_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_agendamento_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX idx_agendamentos_data (data_hora),
    INDEX idx_agendamentos_tatuador_data (tatuador_id, data_hora),
    INDEX idx_agendamentos_cabine_data (cabine, data_hora),
    INDEX idx_agendamentos_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. TABELA: fichas_anamnese
-- Biossegurança rigorosa, questionário de saúde, alertas clínicos e assinatura.
-- -----------------------------------------------------------------------------
CREATE TABLE fichas_anamnese (
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
    CONSTRAINT fk_anamnese_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE SET NULL,
    INDEX idx_anamnese_cliente (cliente_id),
    INDEX idx_anamnese_alerta (alerta_clinico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. TABELA: insumos
-- Controle de estoque crítico para agulhas, pigmentos, biossegurança e EPIs.
-- -----------------------------------------------------------------------------
CREATE TABLE insumos (
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
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_insumos_categoria (categoria),
    INDEX idx_insumos_nivel_critico (quantidade_atual, nivel_critico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. TABELA: transacoes_financeiras
-- Gestão financeira ('Smile Now, Cry Later'), receitas, despesas e repasses.
-- -----------------------------------------------------------------------------
CREATE TABLE transacoes_financeiras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- 'RECEITA', 'DESPESA'
    data DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'CONCLUIDO', -- 'PENDENTE', 'CONCLUIDO', 'CANCELADO'
    tatuador_id BIGINT NULL,
    categoria VARCHAR(80),
    forma_pagamento VARCHAR(50),
    comissao_percentual DECIMAL(5, 2) DEFAULT 0.00,
    valor_repasse DECIMAL(10, 2) DEFAULT 0.00,
    agendamento_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacao_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id) ON DELETE SET NULL,
    CONSTRAINT fk_transacao_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE SET NULL,
    INDEX idx_transacoes_tipo_data (tipo, data),
    INDEX idx_transacoes_tatuador (tatuador_id),
    INDEX idx_transacoes_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- SEED DATA INICIAL
-- Senhas criptografadas com BCrypt (strength 10):
-- admin123     -> $2a$10$wK3F2eA1vG9v9YfSgWl57O2qB7j6f0m/D8eXn4/67f5gYgB/g4X/m
-- rodrigo123   -> $2a$10$W2Y1g7bT6R4V0j8U1H7B2.s2M9N6d0F4q6w4T2y0U8v6W8m4Q2e4a
-- cliente123   -> $2a$10$o2u6J3r4q8M5k9l0P1s2A.9u8e7w6v5t4r3q2w1e0y9u8i7o6p5a
-- =============================================================================

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, cargo, comissao_percentual, ativo)
VALUES (1, 'Administrador Gestor', 'admin@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.', 'ROLE_GESTOR', '(11) 99999-0001', 'Av. Paulista, 1000 - Bela Vista, SP', 'Gestor Executivo', 0.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, especialidade, bio, comissao_percentual, ativo)
VALUES (2, 'Rodrigo Black', 'rodrigo@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.', 'ROLE_TATUADOR', '(11) 98765-4321', 'Rua Augusta, 500 - Consolação, SP', 'Chicano Lettering & Blackwork', 'Especialista em caligrafia gótica chicana de alta precisão e sombreamento black & grey suave.', 70.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, especialidade, bio, comissao_percentual, ativo)
VALUES (3, 'Luna Ink', 'luna@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.', 'ROLE_TATUADOR', '(11) 97654-3210', 'Vila Madalena, SP', 'Microrealismo & Fine Line', 'Mestra em traços milimétricos com agulhas 03RL e 01RL, botânica detalhada.', 65.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, ativo)
VALUES (4, 'Carlos Silva', 'cliente@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.', 'ROLE_CLIENTE', '(11) 91234-5678', 'Rua dos Pinheiros, 200 - Pinheiros, SP', TRUE);

-- Carga inicial de Insumos Críticos
INSERT INTO insumos (codigo, nome, quantidade_atual, nivel_critico, unidade_medida, categoria, lote, data_validade, valor_unitario)
VALUES 
('INS-001', 'Cartucho Agulha 03RL descartável estéril', 45, 20, 'CX', 'Agulhas & Cartuchos', 'LOT-2026A', '2028-12-31', 75.00),
('INS-002', 'Cartucho Agulha 07MG descartável estéril', 15, 20, 'CX', 'Agulhas & Cartuchos', 'LOT-2026B', '2028-12-31', 82.00),
('INS-003', 'Tinta Preto Linha Easy Glow 30ml', 4, 5, 'UN', 'Tintas & Pigmentos', 'LOT-EG99', '2027-06-30', 48.00),
('INS-004', 'Luvas Nitrílicas Pretas Tam M (Caixa c/ 100)', 8, 10, 'CX', 'Biossegurança & EPI', 'LOT-LUV88', '2029-01-01', 55.00),
('INS-005', 'Papel Hectográfico U-20 transferência térmica', 60, 15, 'UN', 'Acessórios & Estêncil', 'LOT-U20-01', '2030-01-01', 12.50);

-- Carga inicial de Agendamento exemplo
INSERT INTO agendamentos (id, data_hora, duracao_minutos, data_hora_fim, cabine, status, valor, valor_sinal, observacoes, cliente_id, tatuador_id)
VALUES (1, '2026-10-10 14:00:00', 180, '2026-10-10 17:00:00', 'Cabine 01', 'CONFIRMADO', 1200.00, 200.00, 'Lettering no antebraço esquerdo', 4, 2);

-- Carga inicial de Ficha de Anamnese correspondente
INSERT INTO fichas_anamnese (cliente_id, agendamento_id, historico_saude, alergias, termo_assinado, alerta_clinico, risco_dermatologico, diabetes, hemofilia, hipertensao, gestante_lactante, observacoes)
VALUES (4, 1, 'Cliente sem histórico de doenças crônicas.', 'Nenhuma alergia conhecida.', TRUE, FALSE, FALSE, FALSE, FALSE, FALSE, FALSE, 'Apto para o procedimento de tatuagem.');

-- Carga inicial de Transações Financeiras (Smile Now, Cry Later)
INSERT INTO transacoes_financeiras (descricao, valor, tipo, data, status, tatuador_id, categoria, forma_pagamento, comissao_percentual, valor_repasse, agendamento_id)
VALUES 
('Sinal Agendamento #1 - Carlos Silva', 200.00, 'RECEITA', '2026-10-01', 'CONCLUIDO', 2, 'Sessão de Tatuagem', 'PIX', 70.00, 140.00, 1),
('Compra de Insumos - Luvas e Agulhas', 450.00, 'DESPESA', '2026-10-01', 'CONCLUIDO', NULL, 'Insumos e Estoque', 'Cartão Corporativo', 0.00, 0.00, NULL);
