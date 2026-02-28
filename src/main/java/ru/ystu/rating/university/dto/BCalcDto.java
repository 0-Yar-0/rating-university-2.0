package ru.ystu.rating.university.dto;

public record BCalcDto(
        Long calcResultId,
        Integer year,
        Integer iteration,
        double b11,
        double b12,
        double b13,
        double b21,
        // additional metrics from the document
        double b22,
        double b23,
        double b24,
        double b25,
        double b26,
        double b31,
        double b32,
        double b33,
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

