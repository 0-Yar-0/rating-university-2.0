--liquibase formatted sql
--changeset sergey:0004_add_even_more_metric_columns

-- add name columns for the newest B metrics (B34 and B41..B44)
ALTER TABLE calc_result_name
    ADD COLUMN code_b34 VARCHAR(50) NOT NULL DEFAULT 'B34',
    ADD COLUMN code_b41 VARCHAR(50) NOT NULL DEFAULT 'B41',
    ADD COLUMN code_b42 VARCHAR(50) NOT NULL DEFAULT 'B42',
    ADD COLUMN code_b43 VARCHAR(50) NOT NULL DEFAULT 'B43',
    ADD COLUMN code_b44 VARCHAR(50) NOT NULL DEFAULT 'B44';
