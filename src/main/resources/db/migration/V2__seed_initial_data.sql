-- Seed inicial do ambiente de desenvolvimento. As verificacoes de existencia
-- permitem aplicar esta carga tambem em bancos baselineados que ja tenham dados.

INSERT INTO usuarios (nome, email, senha, role, telefone, endereco, especialidade, bio, cargo, comissao_percentual, ativo, codigo_indicacao)
SELECT seed.nome, seed.email, seed.senha, seed.role, seed.telefone, seed.endereco, seed.especialidade, seed.bio,
       seed.cargo, seed.comissao_percentual, TRUE, seed.codigo_indicacao
FROM (
    SELECT 'Administrador Gestor' AS nome, 'admin@tattos.com' AS email,
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.' AS senha,
           'ROLE_GESTOR' AS role, '(11) 99999-0001' AS telefone,
           'Av. Paulista, 1000 - Bela Vista, SP' AS endereco, NULL AS especialidade, NULL AS bio,
           'Gestor Executivo' AS cargo,
           0.00 AS comissao_percentual, NULL AS codigo_indicacao
    UNION ALL SELECT 'Carlos Gerente', 'gerente@tattos.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_GESTOR', '(11) 99999-0002', 'Rua Oscar Freire, 300 - Jardins, SP',
           NULL, NULL, 'Gerente de Operações', 0.00, NULL
    UNION ALL SELECT 'Mãozinha Leve', 'maozinha@tattos.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_TATUADOR', '(11) 98765-4321', 'Rua Augusta, 500 - Consolação, SP',
           'Microrealismo & Fine Line',
           'Famoso por fazer traços tão leves que o cliente dorme durante a sessão de 4 horas.',
           NULL, 70.00, NULL
    UNION ALL SELECT 'Bracinho Curto', 'bracinhocurto@tattos.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_TATUADOR', '(11) 97654-3210', 'Vila Madalena, SP', 'Old School & Bold Line',
           'Especialista em fechamentos pesados com linhas grossas e cores sólidas.', NULL, 65.00, NULL
    UNION ALL SELECT 'Agulha de Ouro', 'agulhadeouro@tattos.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_TATUADOR', '(11) 96543-2109', 'Pinheiros, SP', 'Realismo Preto e Cinza',
           'Campeão de convenções, especialista em retratos e texturas hiper-realistas.', NULL, 70.00, NULL
    UNION ALL SELECT 'Treme-Treme', 'tremetreme@tattos.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_TATUADOR', '(11) 95432-1098', 'Moema, SP', 'Tribal & Geek',
           'Apesar do apelido irônico, tem a mão mais firme do estúdio para geometria sagrada.', NULL, 60.00, NULL
    UNION ALL SELECT 'Carlos Silva (Cliente Teste)', 'cliente@tattos.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_CLIENTE', '(11) 91234-5678', 'Rua dos Pinheiros, 200 - Pinheiros, SP',
           NULL, NULL, NULL, 0.00, 'TATTO000007'
    UNION ALL SELECT 'Mariana Costa', 'mariana@email.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_CLIENTE', '(11) 92345-6789', 'Alameda Santos, 1000 - Cerqueira César, SP',
           NULL, NULL, NULL, 0.00, 'TATTO000008'
    UNION ALL SELECT 'Roberto "Bob" Mendes', 'bob@email.com',
           '$2a$10$rNzVnwOUyJkXVl.T9/8QvOAUFlxFPGHdf2louNuQ1q/f4b7QeV4N.',
           'ROLE_CLIENTE', '(11) 93456-7890', 'Rua Harmonia, 400 - Vila Madalena, SP',
           NULL, NULL, NULL, 0.00, 'TATTO000009'
) AS seed
LEFT JOIN usuarios existing ON existing.email = seed.email
WHERE existing.id IS NULL;

INSERT INTO insumos (codigo, nome, quantidade_atual, nivel_critico, unidade_medida, categoria, lote, data_validade, valor_unitario)
SELECT seed.codigo, seed.nome, seed.quantidade_atual, seed.nivel_critico, seed.unidade_medida,
       seed.categoria, seed.lote, seed.data_validade, seed.valor_unitario
FROM (
    SELECT 'INS-PAP01' AS codigo, 'Papel Toalha Interfolhado (Faco/Clássico)' AS nome, 15 AS quantidade_atual, 3 AS nivel_critico, 'CAIXA' AS unidade_medida, 'Higiene e Proteção' AS categoria, 'LT-2026-01' AS lote, '2028-12-31' AS data_validade, 45.00 AS valor_unitario
    UNION ALL SELECT 'INS-TIN01', 'Tinta Preta Linha e Sombra (Dynamic / Electric Ink 30ml)', 8, 2, 'UNIDADE', 'Tintas', 'TIN-BLK-99', '2028-06-15', 85.50
    UNION ALL SELECT 'INS-TIN02', 'Tinta Azul Royal (Electric Ink 30ml)', 4, 1, 'UNIDADE', 'Tintas', 'TIN-BLU-42', '2027-10-20', 79.90
    UNION ALL SELECT 'INS-TIN03', 'Tinta Vermelho Linha (Electric Ink 30ml)', 5, 1, 'UNIDADE', 'Tintas', 'TIN-RED-11', '2028-03-10', 79.90
    UNION ALL SELECT 'INS-TIN04', 'Tinta Amarelo Canário (Electric Ink 30ml)', 3, 1, 'UNIDADE', 'Tintas', 'TIN-YEL-05', '2027-11-25', 79.90
    UNION ALL SELECT 'INS-TIN05', 'Tinta Branco Pura (Dynamic 30ml)', 4, 1, 'UNIDADE', 'Tintas', 'TIN-WHT-88', '2028-01-14', 85.50
    UNION ALL SELECT 'INS-TIN06', 'Tinta Verde Folha (Electric Ink 30ml)', 3, 1, 'UNIDADE', 'Tintas', 'TIN-GRN-33', '2027-09-05', 79.90
    UNION ALL SELECT 'INS-TIN07', 'Tinta Rosa Pink (Electric Ink 30ml)', 2, 1, 'UNIDADE', 'Tintas', 'TIN-PNK-77', '2028-05-18', 79.90
    UNION ALL SELECT 'INS-BAT01', 'Batoques para Tinta (Pote P) - Pacote com 100un', 10, 2, 'PACOTE', 'Acessórios', 'BAT-P-100', '2030-01-01', 15.00
    UNION ALL SELECT 'INS-BAT02', 'Batoques para Tinta (Pote M) - Pacote com 100un', 8, 2, 'PACOTE', 'Acessórios', 'BAT-M-100', '2030-01-01', 18.00
    UNION ALL SELECT 'INS-LUV01', 'Luva de Procedimento Nitrílica Preta (Tam. M - Cx c/100)', 6, 2, 'CAIXA', 'Biossegurança', 'LUV-NIT-M', '2029-05-10', 42.00
    UNION ALL SELECT 'INS-LUV02', 'Luva de Procedimento Nitrílica Preta (Tam. G - Cx c/100)', 5, 2, 'CAIXA', 'Biossegurança', 'LUV-NIT-G', '2029-05-10', 42.00
    UNION ALL SELECT 'INS-ALC01', 'Álcool Isopropílico 70% (Frasco 1 Litro)', 5, 2, 'LITRO', 'Limpeza e Antisséptico', 'ALC-70-03', '2027-04-12', 22.50
    UNION ALL SELECT 'INS-AGU01', 'Agulha 7RL (Round Liner) Esterilizada - Caixa c/ 20un', 3, 1, 'CAIXA', 'Agulhas e Cartuchos', 'AGU-7RL-01', '2029-08-30', 65.00
    UNION ALL SELECT 'INS-AGU02', 'Agulha 3RL (Round Liner) Esterilizada - Caixa c/ 20un', 4, 1, 'CAIXA', 'Agulhas e Cartuchos', 'AGU-3RL-02', '2029-08-30', 65.00
    UNION ALL SELECT 'INS-AGU03', 'Agulha 9M1 (Magnum) Esterilizada - Caixa c/ 20un', 3, 1, 'CAIXA', 'Agulhas e Cartuchos', 'AGU-9M1-03', '2029-09-15', 72.00
    UNION ALL SELECT 'INS-VAS01', 'Vaselina Sólida Pura Medicinal (Pote 500g)', 4, 1, 'UNIDADE', 'Lubrificantes', 'VAS-500G-8', '2028-11-05', 28.00
    UNION ALL SELECT 'INS-POM01', 'Pomada Anestésica TIX / TKTX (Bisnaga 10g)', 5, 2, 'UNIDADE', 'Anestésicos e Cuidados', 'POM-TKTX-1', '2027-07-30', 35.00
    UNION ALL SELECT 'INS-FIL01', 'Filme PVC para Proteção de Bancada e Máquina', 8, 2, 'ROLO', 'Proteção', 'PVC-ROLO-10', '2030-01-01', 18.90
    UNION ALL SELECT 'INS-BAR01', 'Plástico Barrier Film para Cabo e Borrifador (Rolo c/ 1200fls)', 2, 1, 'ROLO', 'Biossegurança', 'BAR-FILM-1', '2030-01-01', 95.00
    UNION ALL SELECT 'INS-LIX01', 'Saco de Lixo Branco Infectante 50L (Pacote c/ 100un)', 3, 1, 'PACOTE', 'Biossegurança', 'LIX-INF-50', '2030-01-01', 55.00
    UNION ALL SELECT 'INS-LIX02', 'Saco de Lixo Comum Preto 100L (Pacote c/ 100un)', 4, 1, 'PACOTE', 'Higiene e Proteção', 'LIX-COM-100', '2030-01-01', 48.00
    UNION ALL SELECT 'INS-DES01', 'Caixa para Descarte de Perfurocortantes - Descarpack 3L', 5, 2, 'UNIDADE', 'Biossegurança', 'DESC-3L-09', '2032-12-31', 25.00
    UNION ALL SELECT 'INS-GEL01', 'Vaselina Mousse / Butter para Tatuagem (Pote 240g)', 4, 1, 'UNIDADE', 'Lubrificantes', 'BUTTER-240', '2028-04-10', 65.00
    UNION ALL SELECT 'INS-TRA01', 'Papel Hectográfico / Carbono para decalque (Caixa c/ 10un)', 5, 2, 'CAIXA', 'Transferência', 'CARB-HEC-1', '2030-01-01', 50.00
    UNION ALL SELECT 'INS-GEL02', 'Gel Fixador de Decalque / Transfer (Stencil Stuff 125ml)', 3, 1, 'UNIDADE', 'Transferência', 'STENCIL-12', '2028-10-01', 89.90
    UNION ALL SELECT 'INS-PAP02', 'Folha Sulfite A4 (Pacote com 500 folhas)', 5, 2, 'PACOTE', 'Escritório', 'SULF-A4-500', '2030-01-01', 32.00
    UNION ALL SELECT 'INS-TON01', 'Toner para Impressora (Compatível)', 2, 1, 'UNIDADE', 'Escritório', 'TON-IMP-01', '2030-01-01', 120.00
    UNION ALL SELECT 'INS-TRA02', 'Papel Transfer para Impressora Térmica (Caixa c/ 100fls)', 4, 1, 'CAIXA', 'Transferência', 'TRANS-TERM', '2029-12-31', 95.00
) AS seed
LEFT JOIN insumos existing ON existing.codigo = seed.codigo
WHERE existing.id IS NULL;

INSERT INTO agendamentos (data_hora, duracao_minutos, data_hora_fim, cabine, status, valor, valor_sinal,
                          observacoes, cliente_id, tatuador_id, created_at, updated_at)
SELECT seed.data_hora, seed.duracao_minutos, seed.data_hora_fim, seed.cabine, seed.status, seed.valor, seed.valor_sinal,
       seed.observacoes, cliente.id, tatuador.id, NOW(), NOW()
FROM (
    SELECT '2026-10-10 14:00:00' AS data_hora, 180 AS duracao_minutos, '2026-10-10 17:00:00' AS data_hora_fim,
           'Cabine 01' AS cabine, 'CONFIRMADO' AS status, 1200.00 AS valor, 200.00 AS valor_sinal,
           'Lettering delicado no antebraço esquerdo' AS observacoes
    UNION ALL SELECT '2026-10-12 10:00:00', 240, '2026-10-12 14:00:00', 'Cabine 02', 'AGENDADO', 1800.00, 300.00,
           'Fechamento de antebraço estilo Oriental / Preto e Cinza'
) AS seed
JOIN usuarios cliente ON cliente.email = 'cliente@tattos.com'
JOIN usuarios tatuador ON tatuador.email = 'maozinha@tattos.com'
WHERE NOT EXISTS (
    SELECT 1 FROM agendamentos existing
    WHERE existing.data_hora = seed.data_hora AND existing.cabine = seed.cabine
);

INSERT INTO fichas_anamnese (cliente_id, agendamento_id, historico_saude, alergias, termo_assinado, assinatura_digital,
                             alerta_clinico, risco_dermatologico, diabetes, hemofilia, hipertensao, gestante_lactante,
                             medicamentos_uso, observacoes, data_criacao)
SELECT cliente.id, agendamento.id, seed.historico_saude, seed.alergias, TRUE,
       'data:image/png;base64,iVBORw0KGgoAAAANSUhEUg==', FALSE, FALSE, FALSE, FALSE, FALSE, FALSE,
       seed.medicamentos_uso, seed.observacoes, NOW()
FROM (
    SELECT '2026-10-10 14:00:00' AS data_hora, 'Cabine 01' AS cabine,
           'Cliente sem histórico de doenças crônicas ou queloides.' AS historico_saude,
           'Nenhuma alergia conhecida a latex ou pigmentos.' AS alergias,
           'Nenhum medicamento de uso contínuo.' AS medicamentos_uso,
           'Cliente orientado sobre os cuidados pós-tatuagem.' AS observacoes
    UNION ALL SELECT '2026-10-12 10:00:00', 'Cabine 02',
           'Cliente sem histórico de doenças crônicas.', 'Nenhuma alergia conhecida.',
           'Nenhum.', 'Ficha atualizada para a sessão longa.'
) AS seed
JOIN agendamentos agendamento ON agendamento.data_hora = seed.data_hora AND agendamento.cabine = seed.cabine
JOIN usuarios cliente ON cliente.email = 'cliente@tattos.com'
WHERE agendamento.cliente_id = cliente.id
  AND NOT EXISTS (
      SELECT 1 FROM fichas_anamnese existing WHERE existing.agendamento_id = agendamento.id
  );

INSERT INTO transacoes_financeiras (descricao, valor, tipo, data, status, tatuador_id, categoria, forma_pagamento,
                                    comissao_percentual, valor_repasse, agendamento_id, created_at, updated_at)
SELECT seed.descricao, seed.valor, seed.tipo, seed.data, seed.status, tatuador.id, seed.categoria, seed.forma_pagamento,
       seed.comissao_percentual, seed.valor_repasse, agendamento.id, NOW(), NOW()
FROM (
    SELECT 'Sinal Agendamento #1 - Carlos Silva' AS descricao, 200.00 AS valor, 'RECEITA' AS tipo,
           '2026-10-01' AS data, 'CONCLUIDO' AS status, 'maozinha@tattos.com' AS tatuador_email,
           'Sessão de Tatuagem' AS categoria, 'PIX' AS forma_pagamento, 70.00 AS comissao_percentual,
           140.00 AS valor_repasse, '2026-10-10 14:00:00' AS agendamento_data, 'Cabine 01' AS cabine
    UNION ALL SELECT 'Repasse Parcial / Ajuste de Caixa - Outubro', 1200.00, 'RECEITA',
           '2026-10-02', 'CONCLUIDO', 'maozinha@tattos.com', 'Sessão de Tatuagem', 'Cartão de Crédito',
           70.00, 840.00, '2026-10-10 14:00:00', 'Cabine 01'
    UNION ALL SELECT 'Compra de Insumos - Luvas, Agulhas e Tintas (Fornecedor Geral)', 450.00, 'DESPESA',
           '2026-10-01', 'CONCLUIDO', NULL, 'Insumos e Estoque', 'Cartão Corporativo',
           0.00, 0.00, NULL, NULL
    UNION ALL SELECT 'Manutenção Preventiva de Equipamentos e Esterilização', 150.00, 'DESPESA',
           '2026-10-03', 'CONCLUIDO', NULL, 'Manutenção e Operacional', 'PIX',
           0.00, 0.00, NULL, NULL
) AS seed
LEFT JOIN usuarios tatuador ON tatuador.email = seed.tatuador_email
LEFT JOIN agendamentos agendamento ON agendamento.data_hora = seed.agendamento_data
                                  AND agendamento.cabine = seed.cabine
WHERE NOT EXISTS (
    SELECT 1 FROM transacoes_financeiras existing
    WHERE existing.descricao = seed.descricao AND existing.data = seed.data
);
