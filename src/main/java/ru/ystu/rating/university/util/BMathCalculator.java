package ru.ystu.rating.university.util;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.BCalcDto;
import ru.ystu.rating.university.dto.BParamsDto;

/**
 * Отвечает только за математику класса B:
 * расчёт B11, B12, B13, B21 и суммы.
 */
@Component
public class BMathCalculator {

    /**
     * Посчитать метрики B для одного набора параметров года и итерации.
     *
     * @param p    входные параметры за год
     * @param iter номер итерации
     */
    public BCalcDto computeBForYear(BParamsDto p, int iter) {
        double eNa = nz(p.ENa());
        double eNb = nz(p.ENb());
        double eNc = nz(p.ENc());
        double eb = nz(p.Eb());
        double ec = nz(p.Ec());

        double beta121 = nz(p.beta121());
        double beta122 = nz(p.beta122());
        double beta131 = nz(p.beta131());
        double beta132 = nz(p.beta132());
        double beta211 = nz(p.beta211());
        double beta212 = nz(p.beta212());

        // --- Формулы ---

        // B11_raw = (ENa*100 + ENb*Eb + ENc*Ec) / (ENa + ENb + ENc)
        double b11raw = Normalizer.safeDiv(
                eNa * 100.0 + eNb * eb + eNc * ec,
                eNa + eNb + eNc
        );
        double b11 = Normalizer.clamp01(b11raw, BMetric.B11.vmin, BMetric.B11.vmax) * BMetric.B11.weight;

        // B12_raw = (beta121 / beta122) * 100
        double b12raw = Normalizer.safeDiv(beta121, beta122) * 100.0;
        double b12 = Normalizer.clamp01(b12raw, BMetric.B12.vmin, BMetric.B12.vmax) * BMetric.B12.weight;

        // B13_raw = beta131 / beta132
        double b13raw = Normalizer.safeDiv(beta131, beta132);
        double b13 = Normalizer.clamp01(b13raw, BMetric.B13.vmin, BMetric.B13.vmax) * BMetric.B13.weight;

        // B21_raw = beta211 / beta212
        double b21raw = Normalizer.safeDiv(beta211, beta212);
        double b21 = Normalizer.clamp01(b21raw, BMetric.B21.vmin, BMetric.B21.vmax) * BMetric.B21.weight;

        double totalB = b11 + b12 + b13 + b21;

        return new BCalcDto(
                null,
                p.year(),
                iter,
                b11,
                b12,
                b13,
                b21,
                totalB,
                "А",
                "Б",
                "В",
                "B11",
                "B12",
                "B13",
                "B21"
        );
    }

    private double nz(Double v) {
        return v == null ? 0.0 : v;
    }
}
