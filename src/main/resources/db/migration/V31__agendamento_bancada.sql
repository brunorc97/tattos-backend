ALTER TABLE agendamentos
    ADD COLUMN bancada_id BIGINT NULL,
    ADD CONSTRAINT fk_agendamento_bancada FOREIGN KEY (bancada_id) REFERENCES bancada(id);

-- Backfill: liga agendamentos existentes à bancada de mesmo nome da cabine
UPDATE agendamentos a
JOIN bancada b ON b.nome = a.cabine
SET a.bancada_id = b.id;
