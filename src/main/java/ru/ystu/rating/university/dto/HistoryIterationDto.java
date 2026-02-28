package ru.ystu.rating.university.dto;

import java.util.List;

public record HistoryIterationDto(
        Integer iter,
        List<?> results
) {
}
