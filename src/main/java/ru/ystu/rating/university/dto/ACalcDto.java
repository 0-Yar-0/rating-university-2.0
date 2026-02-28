package ru.ystu.rating.university.dto;

public record ACalcDto(
        Long calcResultId,
        Integer year,
        Integer iteration,
        double PN,
        double DI,
        double KI,
        double sumPoints,
        double totalA
) {
}
