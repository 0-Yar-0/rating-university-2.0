--liquibase formatted sql
--changeset sergey:0003_add_more_metric_columns

-- add name columns for the extended B metrics so that users can rename them
ALTER TABLE calc_result_name
    ADD COLUMN code_b22 VARCHAR(50) NOT NULL DEFAULT 'B22',
    ADD COLUMN code_b23 VARCHAR(50) NOT NULL DEFAULT 'B23',
    ADD COLUMN code_b24 VARCHAR(50) NOT NULL DEFAULT 'B24',
    ADD COLUMN code_b25 VARCHAR(50) NOT NULL DEFAULT 'B25',
    ADD COLUMN code_b26 VARCHAR(50) NOT NULL DEFAULT 'B26',
    ADD COLUMN code_b31 VARCHAR(50) NOT NULL DEFAULT 'B31',
    ADD COLUMN code_b32 VARCHAR(50) NOT NULL DEFAULT 'B32',
    ADD COLUMN code_b33 VARCHAR(50) NOT NULL DEFAULT 'B33';
