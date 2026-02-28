package ru.ystu.rating.university.dto;

import java.util.Map;

/**
 * Generic container for numerical parameters used in the formulas described
 * in the external "Текстовый документ".  Not all keys are required for every
 * formula; missing values are treated as zero.
 * <p>
 * The key names correspond to the variable names that appear in the document,
 * for example "PNo", "PNv", "PNz", "DIo", "ENa" etc.  Consumers can also
 * supply already aggregated quantities such as "NBP" or "B33_o" if
 * preferred.
 */
public record DocumentParamsDto(Map<String, Double> values) {
    public double get(String key) {
        Double v = values.get(key);
        return v == null ? 0.0 : v;
    }
}
