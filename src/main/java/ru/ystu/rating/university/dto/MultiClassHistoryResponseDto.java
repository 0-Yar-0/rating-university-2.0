package ru.ystu.rating.university.dto;

import java.util.List;

public record MultiClassHistoryResponseDto(
        List<HistoryResponseDto> classes
) {
}
