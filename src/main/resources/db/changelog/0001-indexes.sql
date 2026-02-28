--liquibase formatted sql
--changeset sergey:0001_indexes

-- быстрые выборки по пользователю и итерации
CREATE INDEX IF NOT EXISTS idx_data_user_iter ON data(app_user_id, iter);

-- быстрые выборки по пользователю/классу/году/итерации
CREATE INDEX IF NOT EXISTS idx_data_user_class_year_iter
    ON data(app_user_id, class_id, year_data, iter);

-- индекс на последнюю активность
CREATE INDEX IF NOT EXISTS idx_app_user_last_active ON app_user(last_active);

-- ускорение фильтров/поиска по атрибутам
CREATE INDEX IF NOT EXISTS gin_params_set_params
    ON params_set USING gin (params jsonb_path_ops);

CREATE INDEX IF NOT EXISTS gin_calc_result_params
    ON calc_result USING gin (calc_params jsonb_path_ops);