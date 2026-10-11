/* V6 - Fidelização: Indique e Ganhe */
ALTER TABLE usuarios ADD COLUMN codigo_indicacao VARCHAR(20) UNIQUE;
ALTER TABLE agendamentos ADD COLUMN desconto_aplicado NUMERIC(10,2) NOT NULL DEFAULT 0;
