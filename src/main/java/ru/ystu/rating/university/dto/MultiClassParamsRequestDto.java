package ru.ystu.rating.university.dto;

import java.util.List;

public record MultiClassParamsRequestDto(
        List<ClassParamsBlockDto> classes
) {
}
