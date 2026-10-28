-- V15__add_performance_indexes.sql
-- Adiciona índices para otimização de consultas frequentes e paginação

CREATE INDEX idx_agendamentos_cliente_data ON agendamentos (cliente_id, data_hora);
CREATE INDEX idx_transacoes_periodo_status ON transacoes_financeiras (data, status);
CREATE INDEX idx_insumos_nome ON insumos (nome);
