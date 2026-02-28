package ru.ystu.rating.university.dto;

import java.util.List;

public record ClassCalcBlockDto(
        String classType,
        List<BCalcDto> data
) {
}
