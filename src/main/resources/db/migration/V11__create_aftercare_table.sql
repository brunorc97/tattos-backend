-- V11__create_aftercare_table.sql
-- Cria a tabela aftercare que corresponde à entidade com.tattos.model.Aftercare
CREATE TABLE aftercare (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    cliente_id BIGINT NOT NULL,
    agendamento_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    notes TEXT,
    healing_progress INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_aftercare_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_aftercare_agendamento FOREIGN KEY (agendamento_id) REFERENCES agendamentos(id)
);
