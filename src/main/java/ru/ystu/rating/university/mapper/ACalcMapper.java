package ru.ystu.rating.university.mapper;

import ru.ystu.rating.university.dto.ACalcDto;
import ru.ystu.rating.university.model.CalcResultName;

import java.util.HashMap;
import java.util.Map;

/**
 * Маппер для результатов расчёта класса A.  Очень простой, поскольку пока
 * в таблице calc_result_params хранятся только числа, без подписей.
 */
public final class ACalcMapper {
    private ACalcMapper() {}

    public static Map<String, Object> toCalcJson(ACalcDto dto) {
        Map<String, Object> m = new HashMap<>();
        m.put("PN", dto.PN());
        m.put("DI", dto.DI());
        m.put("KI", dto.KI());
        m.put("sumPoints", dto.sumPoints());
        m.put("total", dto.totalA());
        return m;
    }

    public static ACalcDto fromCalcJson(Long calcResultId,
                                        Integer year,
                                        Integer iter,
                                        Map<String, Object> json,
                                        CalcResultName names) {
        return new ACalcDto(
                calcResultId,
                year,
                iter,
                getDouble(json, "PN"),
                getDouble(json, "DI"),
                getDouble(json, "KI"),
                getDouble(json, "sumPoints"),
                getDouble(json, "total")
        );
    }

    private static double getDouble(Map<String, Object> json, String key) {
        Object v = json.get(key);
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }
}
