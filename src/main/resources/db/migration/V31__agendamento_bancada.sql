ALTER TABLE agendamentos
    ADD COLUMN bancada_id BIGINT NULL,
    ADD CONSTRAINT fk_agendamento_bancada FOREIGN KEY (bancada_id) REFERENCES bancada(id);

-- Backfill: liga agendamentos existentes à bancada de mesmo nome da cabine.
-- COLLATE necessário: `cabine` (V1) usa utf8mb4_unicode_ci e `bancada.nome` (V30) herda o default 0900_ai_ci.
UPDATE agendamentos a
JOIN bancada b ON b.nome = a.cabine COLLATE utf8mb4_0900_ai_ci
SET a.bancada_id = b.id;
