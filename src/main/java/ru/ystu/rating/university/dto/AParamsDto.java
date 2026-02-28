package ru.ystu.rating.university.dto;

public record AParamsDto(
        Integer year,
        Double PNo,
        Double PNv,
        Double PNz,
        Double DIo,
        Double DIv,
        Double DIz,
        Double sumPoints
) {
}
