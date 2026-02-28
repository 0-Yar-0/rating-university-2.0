package ru.ystu.rating.university.service;

import org.springframework.stereotype.Service;
import ru.ystu.rating.university.dto.DocumentCalcDto;
import ru.ystu.rating.university.dto.DocumentParamsDto;
import ru.ystu.rating.university.util.Normalizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic engine that knows how to calculate the various formulas that are
 * documented in the provided text file.  All of the algebraic expressions are
 * hard‑coded here; the component is intentionally simple so that new metrics
 * can be added by appending a few lines of code.
 */
@Service
public class DocumentService {

    /**
     * Compute every formula that we know about using the values supplied by the
     * caller.  If an input value is missing it is treated as zero (nz behaviour).
     */
    public DocumentCalcDto computeAll(DocumentParamsDto in) {
        Map<String, Double> out = new HashMap<>();

        // -- helper to fetch params; treat missing or null values as zero
        java.util.function.Function<String, Double> v = k -> {
            Double val = in.get(k);
            return val == null ? 0.0 : val;
        };

        // -----------------------
        // 1. коррективный коэфф. и сводная оценка
        // -----------------------
        double pno = v.apply("PNo");
        double pnv = v.apply("PNv");
        double pnz = v.apply("PNz");
        double pn = 1.0 * pno + 0.25 * pnv + 0.1 * pnz;
        out.put("PN_raw", pn);
        out.put("PN", pn);

        double dio = v.apply("DIo");
        double div = v.apply("DIv");
        double diz = v.apply("DIz");
        double di = 1.0 * dio + 0.25 * div + 0.1 * diz;
        out.put("DI_raw", di);
        out.put("DI", di);

        double ki = 1 + 0.5 * ((pn == 0 ? 0 : di / pn) - 0.2) / 0.8;
        if (ki < 1.0) ki = 1.0;
        out.put("KI", ki);

        double sumPoints = v.apply("sumPoints");
        double overall = sumPoints * ki;
        out.put("TOTAL_RAW", sumPoints);
        out.put("TOTAL", overall);

        // -----------------------
        // 2. основные показатели Б (часть I --- уже реализованы в BMathCalculator)
        //    мы дублируем их здесь so that one service can generate a complete set
        // -----------------------

        // B11
        double ENa = v.apply("ENa");
        double ENb = v.apply("ENb");
        double ENc = v.apply("ENc");
        double Eb = v.apply("Eb");
        double Ec = v.apply("Ec");
        double b11raw = Normalizer.safeDiv(ENa * 100.0 + ENb * Eb + ENc * Ec,
                ENa + ENb + ENc);
        out.put("B11_raw", b11raw);
        double b11 = Normalizer.clamp01(b11raw, 40.0, 90.0) * 23.0;
        out.put("B11", b11);

        // B12
        double beta121 = v.apply("beta121");
        double beta122 = v.apply("beta122");
        double b12raw = Normalizer.safeDiv(beta121, beta122) * 100.0;
        out.put("B12_raw", b12raw);
        double b12 = Normalizer.clamp01(b12raw, 80.0, 100.0) * 3.0;
        out.put("B12", b12);

        // B13
        double beta131 = v.apply("beta131");
        double beta132 = v.apply("beta132");
        double b13raw = Normalizer.safeDiv(beta131, beta132);
        out.put("B13_raw", b13raw);
        double b13 = Normalizer.clamp01(b13raw, 0.0, 0.5) * 4.0;
        out.put("B13", b13);

        // B21
        double beta211 = v.apply("beta211");
        double beta212 = v.apply("beta212");
        double b21raw = Normalizer.safeDiv(beta211, beta212);
        out.put("B21_raw", b21raw);
        double b21 = Normalizer.clamp01(b21raw, 0.0, 1.0) * 2.0;
        out.put("B21", b21);

        // -----------------------
        //  metrics that were missing until now: B22..B33
        // -----------------------

        // B22 = (NMP + 3*(ACP+OPC+ACC)) / NBP
        double nbp = v.apply("NBP");
        double nmp = v.apply("NMP");
        double acp = v.apply("ACP");
        double opc = v.apply("OPC");
        double acc = v.apply("ACC");
        double b22raw = Normalizer.safeDiv(nmp + 3.0 * (acp + opc + acc), nbp);
        out.put("B22_raw", b22raw);
        // coefficient was incorrectly *6.0 previously; weight should be
        // the normalized raw value itself (0‑1) as per document spec.
        double b22 = Normalizer.clamp01(b22raw, 0.0, 0.25);
        out.put("B22", b22);

        // B23 = (0.25*PKP + PPP) / (NP + NOA)
        double pkp = v.apply("PKP");
        double ppp = v.apply("PPP");
        double np = v.apply("NP");
        double noa = v.apply("NOA");
        double b23raw = Normalizer.safeDiv(0.25 * pkp + ppp, np + noa);
        out.put("B23_raw", b23raw);
        double b23 = Normalizer.clamp01(b23raw, 0.0, 0.20) * 6.0;
        out.put("B23", b23);

        // B24 = NAP / PN  (PN is already computed)
        double nap = v.apply("NAP");
        double b24raw = Normalizer.safeDiv(nap, pn);
        out.put("B24_raw", b24raw);
        double b24 = Normalizer.clamp01(b24raw, 0.0, 0.5) * 6.0;
        out.put("B24", b24);

        // B25: caller may supply raw value or underlying numbers
        if (in.values().containsKey("B25_o")) {
            double b25raw = v.apply("B25_o");
            out.put("B25_raw", b25raw);
            double b25 = Normalizer.clamp01(b25raw, 0.0, 40.0) * 1.0;
            out.put("B25", b25);
        }
        // B26: similar
        if (in.values().containsKey("B26_o")) {
            double b26raw = v.apply("B26_o");
            out.put("B26_raw", b26raw);
            double b26 = Normalizer.clamp01(b26raw, 0.0, 40.0) * 1.0;
            out.put("B26", b26);
        }

        // B31
        double ut = v.apply("UT");
        double doVal = v.apply("DO");
        double b31raw = Normalizer.safeDiv(ut, doVal) * 100.0;
        out.put("B31_raw", b31raw);
        double b31 = Normalizer.clamp01(b31raw, 30.0, 85.0) * 13.0;
        out.put("B31", b31);

        // B32
        double N = v.apply("N");
        double Npr = v.apply("Npr");
        double VO = v.apply("VO");
        double PO = v.apply("PO");
        double b32raw = Normalizer.safeDiv(N + VO - PO, Npr) * 100.0;
        out.put("B32_raw", b32raw);
        double b32 = Normalizer.clamp01(b32raw, 75.0, 90.0) * 5.0;
        out.put("B32", b32);

        // B33: caller may precompute or supply B33_o
        if (in.values().containsKey("B33_o")) {
            double b33raw = v.apply("B33_o");
            out.put("B33_raw", b33raw);
            double b33 = Normalizer.clamp01(b33raw, 1.0, 5.0) * 12.0;
            out.put("B33", b33);
        }

        return new DocumentCalcDto(out);
    }
}
