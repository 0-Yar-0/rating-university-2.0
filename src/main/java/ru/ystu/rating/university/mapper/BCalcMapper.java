package ru.ystu.rating.university.mapper;

import ru.ystu.rating.university.dto.BCalcDto;
import ru.ystu.rating.university.model.CalcResultName;

import java.util.HashMap;
import java.util.Map;

/**
 * Маппер для результатов расчёта класса B.
 */
public final class BCalcMapper {

    private BCalcMapper() {
    }

    /**
     * Превращает BCalcDto в JSON для поля calc_params (JSONB).
     * Пример JSON:
     * {
     * "B11": 20.47,
     * "B12":  2.25,
     * "B13":  1.60,
     * "B21":  1.60,
     * "total": 25.92
     * }
     */
    public static Map<String, Object> toCalcJson(BCalcDto dto) {
        Map<String, Object> m = new HashMap<>();
        m.put("B11", dto.b11());
        m.put("B12", dto.b12());
        m.put("B13", dto.b13());
        m.put("B21", dto.b21());
        m.put("B22", dto.b22());
        m.put("B23", dto.b23());
        m.put("B24", dto.b24());
        m.put("B25", dto.b25());
        m.put("B26", dto.b26());
        m.put("B31", dto.b31());
        m.put("B32", dto.b32());
        m.put("B33", dto.b33());
        m.put("B34", dto.b34());
        m.put("B41", dto.b41());
        m.put("B42", dto.b42());
        m.put("B43", dto.b43());
        m.put("B44", dto.b44());
        m.put("total", dto.sumB());
        return m;
    }

    /**
     * Из JSONB calc_params + сущности с названиями метрик собираем BCalcDto для фронта/экспорта.
     *
     * @param year  год (Data.yearData)
     * @param iter  итерация (Data.iter)
     * @param json  JSONB calc_params как Map<String,Object>
     * @param names CalcResultName (может быть null, тогда подписи по умолчанию B11..B21)
     */
    public static BCalcDto fromCalcJson(Long calcResultId,
                                        Integer year,
                                        Integer iter,
                                        Map<String, Object> json,
                                        CalcResultName names) {
        String codeClassA = names != null ? names.getCodeClassA() : "А";
        String codeClassB = names != null ? names.getCodeClassB() : "Б";
        String codeClassV = names != null ? names.getCodeClassV() : "В";

        String codeB11 = names != null ? names.getCodeB11() : "B11";
        String codeB12 = names != null ? names.getCodeB12() : "B12";
        String codeB13 = names != null ? names.getCodeB13() : "B13";
        String codeB21 = names != null ? names.getCodeB21() : "B21";

        return new BCalcDto(
                calcResultId,
                year,
                iter,
                getDouble(json, "B11"),
                getDouble(json, "B12"),
                getDouble(json, "B13"),
                getDouble(json, "B21"),
                getDouble(json, "B22"),
                getDouble(json, "B23"),
                getDouble(json, "B24"),
                getDouble(json, "B25"),
                getDouble(json, "B26"),
                getDouble(json, "B31"),
                getDouble(json, "B32"),
                getDouble(json, "B33"),
                getDouble(json, "B34"),
                getDouble(json, "B41"),
                getDouble(json, "B42"),
                getDouble(json, "B43"),
                getDouble(json, "B44"),
                getDouble(json, "total"),
                codeClassA,
                codeClassB,
                codeClassV,
                codeB11,
                codeB12,
                codeB13,
                codeB21,
                codeB22,
                codeB23,
                codeB24,
                codeB25,
                codeB26,
                codeB31,
                codeB32,
                codeB33,
                codeB34,
                codeB41,
                codeB42,
                codeB43,
                codeB44
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

