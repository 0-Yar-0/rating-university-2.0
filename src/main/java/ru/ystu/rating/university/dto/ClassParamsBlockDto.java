package ru.ystu.rating.university.dto;

import java.util.List;

public record ClassParamsBlockDto(
        String classType,
        List<?> data,
        BMetricNamesDto names
) {
}
