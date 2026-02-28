package ru.ystu.rating.university.dto;

import java.util.List;

public record HistoryResponseDto(
        String classType,
        List<HistoryIterationDto> items
) {
}
