package ru.ystu.rating.university.dto;

public record BCalcDto(
        Long calcResultId,
        Integer year,
        Integer iteration,
        double b11,
        double b12,
        double b13,
        double b21,
        double sumB,
        String codeClassA,
        String codeClassB,
        String codeClassV,
        String codeB11,
        String codeB12,
        String codeB13,
        String codeB21
) {
}

