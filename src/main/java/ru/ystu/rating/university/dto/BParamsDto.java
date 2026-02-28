package ru.ystu.rating.university.dto;

public record BParamsDto(
        Integer year,
        // original B11..B21 inputs
        Double ENa,
        Double ENb,
        Double ENc,
        Double Eb,
        Double Ec,
        Double beta121,
        Double beta122,
        Double beta131,
        Double beta132,
        Double beta211,
        Double beta212,
        // additional inputs for extended metrics
        Double NBP,
        Double NMP,
        Double ACP,
        Double OPC,
        Double ACC,
        Double PKP,
        Double PPP,
        Double NP,
        Double NOA,
        Double NAP,
        Double B25_o,
        Double B26_o,
        Double UT,
        Double DO,
        Double N,
        Double Npr,
        Double VO,
        Double PO,
        Double B33_o
) {
}

