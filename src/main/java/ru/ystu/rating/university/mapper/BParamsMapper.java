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
        // extended inputs might be null, put only non-null entries
        putIfNotNull(m, "NBP", dto.NBP());
        putIfNotNull(m, "NMP", dto.NMP());
        putIfNotNull(m, "ACP", dto.ACP());
        putIfNotNull(m, "OPC", dto.OPC());
        putIfNotNull(m, "ACC", dto.ACC());
        putIfNotNull(m, "PKP", dto.PKP());
        putIfNotNull(m, "PPP", dto.PPP());
        putIfNotNull(m, "NP", dto.NP());
        putIfNotNull(m, "NOA", dto.NOA());
        putIfNotNull(m, "NAP", dto.NAP());
        putIfNotNull(m, "B25_o", dto.B25_o());
        putIfNotNull(m, "B26_o", dto.B26_o());
        putIfNotNull(m, "UT", dto.UT());
        putIfNotNull(m, "DO", dto.DO());
        putIfNotNull(m, "N", dto.N());
        putIfNotNull(m, "Npr", dto.Npr());
        putIfNotNull(m, "VO", dto.VO());
        putIfNotNull(m, "PO", dto.PO());
        putIfNotNull(m, "B33_o", dto.B33_o());
        // new indicators for extended metrics
        putIfNotNull(m, "NR2023", dto.NR2023());
        putIfNotNull(m, "NR2024", dto.NR2024());
        putIfNotNull(m, "NR2025", dto.NR2025());
        putIfNotNull(m, "WL2022", dto.WL2022());
        putIfNotNull(m, "WL2023", dto.WL2023());
        putIfNotNull(m, "WL2024", dto.WL2024());
        putIfNotNull(m, "NPR2022", dto.NPR2022());
        putIfNotNull(m, "NPR2023", dto.NPR2023());
        putIfNotNull(m, "NPR2024", dto.NPR2024());
        putIfNotNull(m, "DN2022", dto.DN2022());
        putIfNotNull(m, "DN2023", dto.DN2023());
        putIfNotNull(m, "DN2024", dto.DN2024());
        putIfNotNull(m, "Io", dto.Io());
        putIfNotNull(m, "Iv", dto.Iv());
        putIfNotNull(m, "Iz", dto.Iz());
        putIfNotNull(m, "No", dto.No());
        putIfNotNull(m, "Nv", dto.Nv());
        putIfNotNull(m, "Nz", dto.Nz());
        putIfNotNull(m, "OD2022", dto.OD2022());
        putIfNotNull(m, "OD2023", dto.OD2023());
        putIfNotNull(m, "OD2024", dto.OD2024());
        putIfNotNull(m, "PN2022", dto.PN2022());
        putIfNotNull(m, "PN2023", dto.PN2023());
        putIfNotNull(m, "PN2024", dto.PN2024());
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
                getDouble(json, "beta212"),
                getDouble(json, "NBP"),
                getDouble(json, "NMP"),
                getDouble(json, "ACP"),
                getDouble(json, "OPC"),
                getDouble(json, "ACC"),
                getDouble(json, "PKP"),
                getDouble(json, "PPP"),
                getDouble(json, "NP"),
                getDouble(json, "NOA"),
                getDouble(json, "NAP"),
                getDouble(json, "B25_o"),
                getDouble(json, "B26_o"),
                getDouble(json, "UT"),
                getDouble(json, "DO"),
                getDouble(json, "N"),
                getDouble(json, "Npr"),
                getDouble(json, "VO"),
                getDouble(json, "PO"),
                getDouble(json, "B33_o"),
                getDouble(json, "NR2023"),
                getDouble(json, "NR2024"),
                getDouble(json, "NR2025"),
                getDouble(json, "WL2022"),
                getDouble(json, "WL2023"),
                getDouble(json, "WL2024"),
                getDouble(json, "NPR2022"),
                getDouble(json, "NPR2023"),
                getDouble(json, "NPR2024"),
                getDouble(json, "DN2022"),
                getDouble(json, "DN2023"),
                getDouble(json, "DN2024"),
                getDouble(json, "Io"),
                getDouble(json, "Iv"),
                getDouble(json, "Iz"),
                getDouble(json, "No"),
                getDouble(json, "Nv"),
                getDouble(json, "Nz"),
                getDouble(json, "OD2022"),
                getDouble(json, "OD2023"),
                getDouble(json, "OD2024"),
                getDouble(json, "PN2022"),
                getDouble(json, "PN2023"),
                getDouble(json, "PN2024")
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

    private static void putIfNotNull(Map<String, Object> m, String key, Double value) {
        if (value != null) {
            m.put(key, value);
        }
    }
}

