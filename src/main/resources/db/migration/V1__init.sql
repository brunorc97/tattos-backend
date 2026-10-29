-- =============================================================================
-- TattOS ERP - Database Initialization Script (MySQL 8.4)
-- Version: V1__init.sql
-- =============================================================================

SET
FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS transacoes_financeiras;
DROP TABLE IF EXISTS fichas_anamnese;
DROP TABLE IF EXISTS agendamentos;
DROP TABLE IF EXISTS insumos;
DROP TABLE IF EXISTS usuarios;

SET
FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 1. TABELA: usuarios
-- Armazena todos os usuários do sistema com herança / atributos para:
-- Gestor (ROLE_GESTOR), Tatuador (ROLE_TATUADOR) e Cliente (ROLE_CLIENTE).
-- -----------------------------------------------------------------------------
CREATE TABLE usuarios
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome                VARCHAR(150) NOT NULL,
    email               VARCHAR(150) NOT NULL UNIQUE,
    senha               VARCHAR(255) NOT NULL,
    role                VARCHAR(50)  NOT NULL,
    telefone            VARCHAR(30),
    endereco            VARCHAR(255),
    especialidade       VARCHAR(100),
    bio                 TEXT,
    cargo               VARCHAR(100),
    comissao_percentual DECIMAL(5, 2)         DEFAULT 0.00,
    ativo               BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX               idx_usuarios_email (email),
    INDEX               idx_usuarios_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. TABELA: agendamentos
-- Gerenciamento de sessões com prevenção de conflitos de agenda e cabines.
-- -----------------------------------------------------------------------------
CREATE TABLE agendamentos
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_hora       DATETIME       NOT NULL,
    duracao_minutos INT            NOT NULL DEFAULT 60,
    data_hora_fim   DATETIME       NOT NULL,
    cabine          VARCHAR(50)    NOT NULL,
    status          VARCHAR(30)    NOT NULL DEFAULT 'AGENDADO',
    valor           DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    valor_sinal     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    observacoes     TEXT,
    cliente_id      BIGINT         NOT NULL,
    tatuador_id     BIGINT         NOT NULL,
    created_at      TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_agendamento_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_agendamento_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
    INDEX           idx_agendamentos_data (data_hora),
    INDEX           idx_agendamentos_tatuador_data (tatuador_id, data_hora),
    INDEX           idx_agendamentos_cabine_data (cabine, data_hora),
    INDEX           idx_agendamentos_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. TABELA: fichas_anamnese
-- Biossegurança rigorosa, questionário de saúde, alertas clínicos e assinatura.
-- -----------------------------------------------------------------------------
CREATE TABLE fichas_anamnese
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id          BIGINT  NOT NULL,
    agendamento_id      BIGINT NULL,
    historico_saude     TEXT,
    alergias            TEXT,
    termo_assinado      BOOLEAN NOT NULL DEFAULT FALSE,
    assinatura_digital  LONGTEXT,
    alerta_clinico      BOOLEAN NOT NULL DEFAULT FALSE,
    risco_dermatologico BOOLEAN NOT NULL DEFAULT FALSE,
    diabetes            BOOLEAN NOT NULL DEFAULT FALSE,
    hemofilia           BOOLEAN NOT NULL DEFAULT FALSE,
    hipertensao         BOOLEAN NOT NULL DEFAULT FALSE,
    gestante_lactante   BOOLEAN NOT NULL DEFAULT FALSE,
    medicamentos_uso    TEXT,
    observacoes         TEXT,
    data_criacao        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_anamnese_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_anamnese_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE SET NULL,
    INDEX               idx_anamnese_cliente (cliente_id),
    INDEX               idx_anamnese_alerta (alerta_clinico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. TABELA: insumos
-- Controle de estoque crítico para agulhas, pigmentos, biossegurança e EPIs.
-- -----------------------------------------------------------------------------
CREATE TABLE insumos
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo           VARCHAR(30) UNIQUE,
    nome             VARCHAR(150) NOT NULL,
    quantidade_atual INT          NOT NULL DEFAULT 0,
    nivel_critico    INT          NOT NULL DEFAULT 10,
    unidade_medida   VARCHAR(30)  NOT NULL DEFAULT 'UN',
    categoria        VARCHAR(80),
    lote             VARCHAR(50),
    data_validade    DATE,
    valor_unitario   DECIMAL(10, 2)        DEFAULT 0.00,
    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX            idx_insumos_categoria (categoria),
    INDEX            idx_insumos_nivel_critico (quantidade_atual, nivel_critico)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. TABELA: transacoes_financeiras
-- Gestão financeira ('Smile Now, Cry Later'), receitas, despesas e repasses.
-- -----------------------------------------------------------------------------
CREATE TABLE transacoes_financeiras
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao           VARCHAR(255)   NOT NULL,
    valor               DECIMAL(10, 2) NOT NULL,
    tipo                VARCHAR(20)    NOT NULL,                     -- 'RECEITA', 'DESPESA'
    data                DATE           NOT NULL,
    status              VARCHAR(30)    NOT NULL DEFAULT 'CONCLUIDO', -- 'PENDENTE', 'CONCLUIDO', 'CANCELADO'
    tatuador_id         BIGINT NULL,
    categoria           VARCHAR(80),
    forma_pagamento     VARCHAR(50),
    comissao_percentual DECIMAL(5, 2)           DEFAULT 0.00,
    valor_repasse       DECIMAL(10, 2)          DEFAULT 0.00,
    agendamento_id      BIGINT NULL,
    created_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transacao_tatuador FOREIGN KEY (tatuador_id) REFERENCES usuarios (id) ON DELETE SET NULL,
    CONSTRAINT fk_transacao_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos (id) ON DELETE SET NULL,
    INDEX               idx_transacoes_tipo_data (tipo, data),
    INDEX               idx_transacoes_tatuador (tatuador_id),
    INDEX               idx_transacoes_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

