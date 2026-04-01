--liquibase formatted sql
--changeset sergey:0002_insert_tables

INSERT INTO classes(code) VALUES
('A'),
('B'),
('V');

INSERT INTO roles(name) VALUES
('ADMIN'),
('USER');

INSERT INTO app_user(name, email, password_hash, role_id, last_active) VALUES
('admin', 'admin@email.ru', '$2a$10$leRsVyFIdcAe5sHhmuPuWuPycjXQZz4CvDwsPAkQiTac6wil8/EzW', 1, '2025-11-18 02:05:00.253008+03')
