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
    comissao_percentual DECIMAL(5, 2)         DEFAULT 70.00,
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

-- =============================================================================

--### 🔑 Contas de Acesso para Testes (Seed)

-- Todas as contas utilizam o hash BCrypt padrão para a senha de teste (`admin123` ou equivalente configurado no banco).

-- | ID | Nome / Apelido | E-mail de Login | Perfil / Cargo | Senha (Plain) | Senha Criptografada (BCrypt) |
-- | :--- | :--- | :--- | :--- | :--- | :--- |
-- | **1** | Administrador Gestor | `admin@tattos.com` | **GESTOR** (Gestor Executivo) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **2** | Carlos Gerente | `gerente@tattos.com` | **GESTOR** (Gerente de Operações) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **3** | Mãozinha Leve | `maozinha@tattos.com` | **TATUADOR** (Microrealismo & Fine Line) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **4** | Bracinho Curto | `bracinhocurto@tattos.com` | **TATUADOR** (Old School & Bold Line) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **5** | Agulha de Ouro | `agulhadeouro@tattos.com` | **TATUADOR** (Realismo Preto e Cinza) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **6** | Treme-Treme | `tremetreme@tattos.com` | **TATUADOR** (Tribal & Geek) | `admin123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **7** | Carlos Silva | `cliente@tattos.com` | **CLIENTE** | `cliente123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **8** | Mariana Costa | `mariana@email.com` | **CLIENTE** | `cliente123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |
-- | **9** | Roberto "Bob" Mendes | `bob@email.com` | **CLIENTE** | `cliente123` | `$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.` |

-- =============================================================================

-- Gestores
INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, cargo, comissao_percentual, ativo)
VALUES (1, 'Administrador Gestor', 'admin@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_GESTOR', '(11) 99999-0001', 'Av. Paulista, 1000 - Bela Vista, SP', 'Gestor Executivo', 0.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, cargo, comissao_percentual, ativo)
VALUES (2, 'Carlos Gerente', 'gerente@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_GESTOR', '(11) 99999-0002', 'Rua Oscar Freire, 300 - Jardins, SP', 'Gerente de Operações', 0.00, TRUE);

-- Tatuadores (com apelidos criativos e engraçados)
INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, especialidade, bio, comissao_percentual, ativo)
VALUES (3, 'Mãozinha Leve', 'maozinha@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_TATUADOR', '(11) 98765-4321', 'Rua Augusta, 500 - Consolação, SP', 'Microrealismo & Fine Line',
        'Famoso por fazer traços tão leves que o cliente dorme durante a sessão de 4 horas.', 70.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, especialidade, bio, comissao_percentual, ativo)
VALUES (4, 'Bracinho Curto', 'bracinhocurto@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_TATUADOR', '(11) 97654-3210', 'Vila Madalena, SP', 'Old School & Bold Line',
        'Especialista em fechamentos pesados com linhas grossas e cores sólidas.', 65.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, especialidade, bio, comissao_percentual, ativo)
VALUES (5, 'Agulha de Ouro', 'agulhadeouro@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_TATUADOR', '(11) 96543-2109', 'Pinheiros, SP', 'Realismo Preto e Cinza',
        'Campeão de convenções, especialista em retratos e texturas hiper-realistas.', 70.00, TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, especialidade, bio, comissao_percentual, ativo)
VALUES (6, 'Treme-Treme', 'tremetreme@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_TATUADOR', '(11) 95432-1098', 'Moema, SP', 'Tribal & Geek',
        'Apesar do apelido irônico, tem a mão mais firme do estúdio para geometria sagrada.', 60.00, TRUE);

-- Clientes
INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, ativo)
VALUES (7, 'Carlos Silva (Cliente Teste)', 'cliente@tattos.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_CLIENTE', '(11) 91234-5678', 'Rua dos Pinheiros, 200 - Pinheiros, SP', TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, ativo)
VALUES (8, 'Mariana Costa', 'mariana@email.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_CLIENTE', '(11) 92345-6789', 'Alameda Santos, 1000 - Cerqueira César, SP', TRUE);

INSERT INTO usuarios (id, nome, email, senha, role, telefone, endereco, ativo)
VALUES (9, 'Roberto "Bob" Mendes', 'bob@email.com', '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
        'ROLE_CLIENTE', '(11) 93456-7890', 'Rua Harmonia, 400 - Vila Madalena, SP', TRUE);

-- Carga inicial de Insumos Críticos
INSERT INTO insumos (codigo, nome, quantidade_atual, nivel_critico, unidade_medida, categoria, lote, data_validade,
                     valor_unitario, created_at, updated_at)
VALUES ('INS-PAP01', 'Papel Toalha Interfolhado (Faco/Clássico)', 15, 3, 'CAIXA', 'Higiene e Proteção', 'LT-2026-01',
        '2028-12-31', 45.00, NOW(), NOW()),
       ('INS-TIN01', 'Tinta Preta Linha e Sombra (Dynamic / Electric Ink 30ml)', 8, 2, 'UNIDADE', 'Tintas',
        'TIN-BLK-99', '2028-06-15', 85.50, NOW(), NOW()),
       ('INS-TIN02', 'Tinta Azul Royal (Electric Ink 30ml)', 4, 1, 'UNIDADE', 'Tintas', 'TIN-BLU-42', '2027-10-20',
        79.90, NOW(), NOW()),
       ('INS-TIN03', 'Tinta Vermelho Linha (Electric Ink 30ml)', 5, 1, 'UNIDADE', 'Tintas', 'TIN-RED-11', '2028-03-10',
        79.90, NOW(), NOW()),
       ('INS-TIN04', 'Tinta Amarelo Canário (Electric Ink 30ml)', 3, 1, 'UNIDADE', 'Tintas', 'TIN-YEL-05', '2027-11-25',
        79.90, NOW(), NOW()),
       ('INS-TIN05', 'Tinta Branco Pura (Dynamic 30ml)', 4, 1, 'UNIDADE', 'Tintas', 'TIN-WHT-88', '2028-01-14', 85.50,
        NOW(), NOW()),
       ('INS-TIN06', 'Tinta Verde Folha (Electric Ink 30ml)', 3, 1, 'UNIDADE', 'Tintas', 'TIN-GRN-33', '2027-09-05',
        79.90, NOW(), NOW()),
       ('INS-TIN07', 'Tinta Rosa Pink (Electric Ink 30ml)', 2, 1, 'UNIDADE', 'Tintas', 'TIN-PNK-77', '2028-05-18',
        79.90, NOW(), NOW()),
       ('INS-BAT01', 'Batoques para Tinta (Pote P) - Pacote com 100un', 10, 2, 'PACOTE', 'Acessórios', 'BAT-P-100',
        '2030-01-01', 15.00, NOW(), NOW()),
       ('INS-BAT02', 'Batoques para Tinta (Pote M) - Pacote com 100un', 8, 2, 'PACOTE', 'Acessórios', 'BAT-M-100',
        '2030-01-01', 18.00, NOW(), NOW()),
       ('INS-LUV01', 'Luva de Procedimento Nitrílica Preta (Tam. M - Cx c/100)', 6, 2, 'CAIXA', 'Biossegurança',
        'LUV-NIT-M', '2029-05-10', 42.00, NOW(), NOW()),
       ('INS-LUV02', 'Luva de Procedimento Nitrílica Preta (Tam. G - Cx c/100)', 5, 2, 'CAIXA', 'Biossegurança',
        'LUV-NIT-G', '2029-05-10', 42.00, NOW(), NOW()),
       ('INS-ALC01', 'Álcool Isopropílico 70% (Frasco 1 Litro)', 5, 2, 'LITRO', 'Limpeza e Antisséptico', 'ALC-70-03',
        '2027-04-12', 22.50, NOW(), NOW()),
       ('INS-AGU01', 'Agulha 7RL (Round Liner) Esterilizada - Caixa c/ 20un', 3, 1, 'CAIXA', 'Agulhas e Cartuchos',
        'AGU-7RL-01', '2029-08-30', 65.00, NOW(), NOW()),
       ('INS-AGU02', 'Agulha 3RL (Round Liner) Esterilizada - Caixa c/ 20un', 4, 1, 'CAIXA', 'Agulhas e Cartuchos',
        'AGU-3RL-02', '2029-08-30', 65.00, NOW(), NOW()),
       ('INS-AGU03', 'Agulha 9M1 (Magnum) Esterilizada - Caixa c/ 20un', 3, 1, 'CAIXA', 'Agulhas e Cartuchos',
        'AGU-9M1-03', '2029-09-15', 72.00, NOW(), NOW()),
       ('INS-VAS01', 'Vaselina Sólida Pura Medicinal (Pote 500g)', 4, 1, 'UNIDADE', 'Lubrificantes', 'VAS-500G-8',
        '2028-11-05', 28.00, NOW(), NOW()),
       ('INS-POM01', 'Pomada Anestésica TIX / TKTX (Bisnaga 10g)', 5, 2, 'UNIDADE', 'Anestésicos e Cuidados',
        'POM-TKTX-1', '2027-07-30', 35.00, NOW(), NOW()),
       ('INS-FIL01', 'Filme PVC para Proteção de Bancada e Máquina', 8, 2, 'ROLO', 'Proteção', 'PVC-ROLO-10',
        '2030-01-01', 18.90, NOW(), NOW()),
       ('INS-BAR01', 'Plástico Barrier Film para Cabo e Borrifador (Rolo c/ 1200fls)', 2, 1, 'ROLO', 'Biossegurança',
        'BAR-FILM-1', '2030-01-01', 95.00, NOW(), NOW()),
       ('INS-LIX01', 'Saco de Lixo Branco Infectante 50L (Pacote c/ 100un)', 3, 1, 'PACOTE', 'Biossegurança',
        'LIX-INF-50', '2030-01-01', 55.00, NOW(), NOW()),
       ('INS-LIX02', 'Saco de Lixo Comum Preto 100L (Pacote c/ 100un)', 4, 1, 'PACOTE', 'Higiene e Proteção',
        'LIX-COM-100', '2030-01-01', 48.00, NOW(), NOW()),
       ('INS-DES01', 'Caixa para Descarte de Perfurocortantes - Descarpack 3L', 5, 2, 'UNIDADE', 'Biossegurança',
        'DESC-3L-09', '2032-12-31', 25.00, NOW(), NOW()),
       ('INS-GEL01', 'Vaselina Mousse / Butter para Tatuagem (Pote 240g)', 4, 1, 'UNIDADE', 'Lubrificantes',
        'BUTTER-240', '2028-04-10', 65.00, NOW(), NOW()),
       ('INS-TRA01', 'Papel Hectográfico / Carbono para decalque (Caixa c/ 10un)', 5, 2, 'CAIXA', 'Transferência',
        'CARB-HEC-1', '2030-01-01', 50.00, NOW(), NOW()),
       ('INS-GEL02', 'Gel Fixador de Decalque / Transfer (Stencil Stuff 125ml)', 3, 1, 'UNIDADE', 'Transferência',
        'STENCIL-12', '2028-10-01', 89.90, NOW(), NOW()),
       ('INS-PAP02', 'Folha Sulfite A4 (Pacote com 500 folhas)', 5, 2, 'PACOTE', 'Escritório', 'SULF-A4-500',
        '2030-01-01', 32.00, NOW(), NOW()),
       ('INS-TON01', 'Toner para Impressora (Compatível)', 2, 1, 'UNIDADE', 'Escritório', 'TON-IMP-01', '2030-01-01',
        120.00, NOW(), NOW()),
       ('INS-TRA02', 'Papel Transfer para Impressora Térmica (Caixa c/ 100fls)', 4, 1, 'CAIXA', 'Transferência',
        'TRANS-TERM', '2029-12-31', 95.00, NOW(), NOW());


-- 1. Carga inicial de Agendamentos (com datas ajustadas para o fluxo operacional)
INSERT INTO agendamentos (id, data_hora, duracao_minutos, data_hora_fim, cabine, status, valor, valor_sinal,
                          observacoes, cliente_id, tatuador_id, created_at, updated_at)
VALUES
    (1, '2026-10-10 14:00:00', 180, '2026-10-10 17:00:00', 'Cabine 01', 'CONFIRMADO', 1200.00, 200.00,
     'Lettering delicado no antebraço esquerdo', 4, 2, NOW(), NOW()),
    (2, '2026-10-12 10:00:00', 240, '2026-10-12 14:00:00', 'Cabine 02', 'AGENDADO', 1800.00, 300.00,
     'Fechamento de antebraço estilo Oriental / Preto e Cinza', 4, 2, NOW(), NOW());

-- 2. Carga inicial de Fichas de Anamnese correspondentes
INSERT INTO fichas_anamnese (cliente_id, agendamento_id, historico_saude, alergias, termo_assinado, assinaturaDigital, alerta_clinico,
                             risco_dermatologico, diabetes, hemofilia, hipertensao, gestante_lactante, medicamentosUso, observacoes, data_criacao)
VALUES
    (4, 1, 'Cliente sem histórico de doenças crônicas ou keloides.', 'Nenhuma alergia conhecida a latex ou pigmentos.', TRUE, 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==', FALSE, FALSE, FALSE,
     FALSE, FALSE, FALSE, 'Nenhum medicamento de uso contínuo.', 'Cliente orientado sobre os cuidados pós-tatuagem.', NOW()),
    (4, 2, 'Cliente sem histórico de doenças crônicas.', 'Nenhuma alergia conhecida.', TRUE, 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==', FALSE, FALSE, FALSE,
     FALSE, FALSE, FALSE, 'Nenhum.', 'Ficha atualizada para a sessão longa.', NOW());

-- 3. Carga inicial de Transações Financeiras (Receitas de sinal/sessões e despesas com insumos/estrutura)
INSERT INTO transacoes_financeiras (descricao, valor, tipo, data, status, tatuador_id, categoria, forma_pagamento,
                                    comissao_percentual, valor_repasse, agendamento_id, created_at, updated_at)
VALUES
    ('Sinal Agendamento #1 - Carlos Silva', 200.00, 'RECEITA', '2026-10-01', 'CONCLUIDO', 2, 'Sessão de Tatuagem',
     'PIX', 70.00, 140.00, 1, NOW(), NOW()),
    ('Repasse Parcial / Ajuste de Caixa - Outubro', 1200.00, 'RECEITA', '2026-10-02', 'CONCLUIDO', 2, 'Sessão de Tatuagem',
     'Cartão de Crédito', 70.00, 840.00, 1, NOW(), NOW()),
    ('Compra de Insumos - Luvas, Agulhas e Tintas (Fornecedor Geral)', 450.00, 'DESPESA', '2026-10-01', 'CONCLUIDO', NULL, 'Insumos e Estoque',
     'Cartão Corporativo', 0.00, 0.00, NULL, NOW(), NOW()),
    ('Manutenção Preventiva de Equipamentos e Esterilização', 150.00, 'DESPESA', '2026-10-03', 'CONCLUIDO', NULL, 'Manutenção e Operacional',
     'PIX', 0.00, 0.00, NULL, NOW(), NOW());
