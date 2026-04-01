--liquibase formatted sql
--changeset sergey:0005_add_m_class

INSERT INTO classes(code)
SELECT 'M'
WHERE NOT EXISTS (
    SELECT 1 FROM classes WHERE code = 'M'
);
