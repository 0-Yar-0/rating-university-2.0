package ru.ystu.rating.university.dto;

import java.util.Map;

/**
 * Result of running formulas over a set of parameters.  The map keys are the
 * metric names ("PN", "DI", "KI", "B11", "B22", etc.) and the values
 * are the computed (weighted) scores.  Raw intermediate values are also
 * available under keys ending with "_raw".
 */
public record DocumentCalcDto(Map<String, Double> values) {
    public double get(String key) {
        Double v = values.get(key);
        return v == null ? 0.0 : v;
    }
}
