--liquibase formatted sql
--changeset sergey:0000_init_tables

-- таблица классов
CREATE TABLE IF NOT EXISTS classes (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(1) NOT NULL UNIQUE
);

-- таблица ролей пользователей
CREATE TABLE IF NOT EXISTS roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- таблица пользователей
CREATE TABLE IF NOT EXISTS app_user (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(90) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role_id       BIGINT NOT NULL,
    last_active   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- таблица текущей итерации пользователя
CREATE TABLE IF NOT EXISTS user_iter_state (
    id           BIGSERIAL PRIMARY KEY,
    app_user_id  BIGINT NOT NULL,
    current_iter INTEGER,

    FOREIGN KEY (app_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_iter_state_user UNIQUE (app_user_id)
);


-- Данные передаваемые в запросе
CREATE TABLE IF NOT EXISTS data (
    id           BIGSERIAL PRIMARY KEY,
    app_user_id  BIGINT NOT NULL,
    class_id     BIGINT NOT NULL,
    iter         INTEGER NOT NULL,
    year_data    INTEGER NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    FOREIGN KEY (app_user_id) REFERENCES app_user(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id)    REFERENCES classes(id)  ON DELETE CASCADE,

    CONSTRAINT uq_data_user_class_year_iter UNIQUE (app_user_id, class_id, year_data, iter)
);

-- таблица вводимых параметров
CREATE TABLE IF NOT EXISTS params_set (
    id      BIGSERIAL PRIMARY KEY,
    params  JSONB NOT NULL,
    data_id BIGINT NOT NULL UNIQUE,

    FOREIGN KEY (data_id) REFERENCES data(id) ON DELETE CASCADE,

    CONSTRAINT ck_params_object CHECK (jsonb_typeof(params) = 'object')
);

-- таблица посчитанных параметров класса
CREATE TABLE IF NOT EXISTS calc_result (
    id          BIGSERIAL PRIMARY KEY,
    calc_params JSONB NOT NUll,
    data_id     BIGINT NOT NULL UNIQUE,

    FOREIGN KEY (data_id) REFERENCES data(id) ON DELETE CASCADE,

    CONSTRAINT ck_calc_object CHECK (jsonb_typeof(calc_params) = 'object')
);

-- таблица наименований метрик
CREATE TABLE IF NOT EXISTS calc_result_name (
    id             BIGSERIAL   PRIMARY KEY,
    calc_result_id BIGINT NOT NULL UNIQUE,
    code_class_a   VARCHAR(50) NOT NULL DEFAULT 'А',
    code_class_b   VARCHAR(50) NOT NULL DEFAULT 'Б',
    code_class_v   VARCHAR(50) NOT NULL DEFAULT 'В',
    code_b11       VARCHAR(50) NOT NULL DEFAULT 'B11',
    code_b12       VARCHAR(50) NOT NULL DEFAULT 'B12',
    code_b13       VARCHAR(50) NOT NULL DEFAULT 'B13',
    code_b21       VARCHAR(50) NOT NULL DEFAULT 'B21',

    FOREIGN KEY (calc_result_id) REFERENCES calc_result(id) ON DELETE CASCADE
);