package ru.ystu.rating.university.mapper;

import ru.ystu.rating.university.dto.BParamsDto;

import java.util.HashMap;
import java.util.Map;

/**
 * Маппер для параметров класса B
 */
public final class BParamsMapper {

    private BParamsMapper() {
    }

    /**
     * Превращаем DTO с параметрами за год в JSON-объект для поля params (JSONB).
     * Это то, что попадёт в колонку params таблицы params_set.
     */
    public static Map<String, Object> toJson(BParamsDto dto) {
        Map<String, Object> m = new HashMap<>();
        m.put("ENa", dto.ENa());
        m.put("ENb", dto.ENb());
        m.put("ENc", dto.ENc());
        m.put("Eb", dto.Eb());
        m.put("Ec", dto.Ec());
        m.put("beta121", dto.beta121());
        m.put("beta122", dto.beta122());
        m.put("beta131", dto.beta131());
        m.put("beta132", dto.beta132());
        m.put("beta211", dto.beta211());
        m.put("beta212", dto.beta212());
        return m;
    }

    /**
     * Обратный маппинг: из JSONB params + года строим DTO.
     * Используется при экспорте или когда отдаём введённые параметры на фронт.
     */
    public static BParamsDto fromJson(Integer year, Map<String, Object> json) {
        return new BParamsDto(
                year,
                getDouble(json, "ENa"),
                getDouble(json, "ENb"),
                getDouble(json, "ENc"),
                getDouble(json, "Eb"),
                getDouble(json, "Ec"),
                getDouble(json, "beta121"),
                getDouble(json, "beta122"),
                getDouble(json, "beta131"),
                getDouble(json, "beta132"),
                getDouble(json, "beta211"),
                getDouble(json, "beta212")
        );
    }

    private static double getDouble(Map<String, Object> json, String key) {
        Object v = json.get(key);
        if (v == null) {
            return 0.0;
        }
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(v.toString());
    }
}

