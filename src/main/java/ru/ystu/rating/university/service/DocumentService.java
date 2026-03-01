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
        double b12 = Normalizer.clamp01(b12raw, 80.0, 100.0);
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
        // where:
        // NBP = 1.0*NBo + 0.25*NBv + 0.1*NBz
        // NMP = 1.0*NMo + 0.25*NMv + 0.1*NMz
        // ACP = 1.0*ACo + 0.25*ACv + 0.1*ACz
        double nbp = 1.0 * v.apply("NBo") + 0.25 * v.apply("NBv") + 0.1 * v.apply("NBz");
        double nmp = 1.0 * v.apply("NMo") + 0.25 * v.apply("NMv") + 0.1 * v.apply("NMz");
        double acp = 1.0 * v.apply("ACo") + 0.25 * v.apply("ACv") + 0.1 * v.apply("ACz");
        double opc = v.apply("OPC");
        double acc = v.apply("ACC");
        double b22raw = Normalizer.safeDiv(nmp + 3.0 * (acp + opc + acc), nbp);
        out.put("B22_raw", b22raw);
        double b22 = Normalizer.clamp01(b22raw, 0.0, 0.25) * 6.0;
        out.put("B22", b22);

        // B23 = (0.25*PKP + PPP) / (NP + NOA)
        // where:
        // NP  = 1.0*No + 0.25*Nv + 0.1*Nz
        // PKP = 1.0*KPo + 0.25*KPv + 0.1*KPz
        // PPP = 1.0*PPPo + 0.25*PPPv + 0.1*PPPz
        double pkp = 1.0 * v.apply("KPo") + 0.25 * v.apply("KPv") + 0.1 * v.apply("KPz");
        double ppp = 1.0 * v.apply("PPPo") + 0.25 * v.apply("PPPv") + 0.1 * v.apply("PPPz");
        double np = 1.0 * v.apply("No") + 0.25 * v.apply("Nv") + 0.1 * v.apply("Nz");
        double noa = v.apply("NOA");
        double b23raw = Normalizer.safeDiv(0.25 * pkp + ppp, np + noa);
        out.put("B23_raw", b23raw);
        double b23 = Normalizer.clamp01(b23raw, 0.0, 0.20) * 6.0;
        out.put("B23", b23);

        // B24 = NAP / PN
        // where:
        // NAP = 1.0*NAo + 0.25*NAv + 0.1*NAz
        // PN  = 1.0*PNo + 0.25*PNv + 0.1*PNz
        double nap = 1.0 * v.apply("NAo") + 0.25 * v.apply("NAv") + 0.1 * v.apply("NAz");
        double pnForB24 = pn;
        double b24raw = Normalizer.safeDiv(nap, pnForB24);
        out.put("B24_raw", b24raw);
        double b24 = Normalizer.clamp01(b24raw, 0.0, 0.5) * 6.0;
        out.put("B24", b24);

        // B25_o = (Σ(ЧПСiY/ЧПiY), Y=2022..2022+k-1) * 100 / k
        int requestedKYears = (int) Math.round(firstPresent(in, "k", "K"));
        int kYears = requestedKYears > 0 ? requestedKYears : 3;
        int startYear = 2022;

        double sum25 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = startYear + i;
            double chps = firstPresent(in,
                "ЧПСi" + year,
                "CHPSi" + year,
                "CPSi" + year);
            double chp = firstPresent(in,
                "ЧПi" + year,
                "CHPi" + year,
                "CPi" + year);
            if (chp > 0) {
            sum25 += chps / chp;
            }
        }
        double b25raw = (sum25 * 100.0) / kYears;
        out.put("B25_raw", b25raw);
        double b25 = Normalizer.clamp01(b25raw, 0.0, 40.0) * 1.0;
        out.put("B25", b25);

        // B26_o = (Σ(ЧОСiY/ЧОiY), Y=2022..2022+k-1) * 100 / k
        double sum26 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = startYear + i;
            double chos = firstPresent(in,
                "ЧОСi" + year,
                "CHOSi" + year,
                "COSi" + year);
            double cho = firstPresent(in,
                "ЧОi" + year,
                "CHOi" + year,
                "COi" + year);
            if (cho > 0) {
            sum26 += chos / cho;
            }
        }
        double b26raw = (sum26 * 100.0) / kYears;
        out.put("B26_raw", b26raw);
        double b26 = Normalizer.clamp01(b26raw, 0.0, 40.0) * 1.0;
        out.put("B26", b26);

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

        // --------------------------------------------------
        // B34 — востребованность на рынке труда
        // B34_raw = (Σ NR[Y], Y=2023..2023+k-1) / k
        double sum34 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2023 + i;
            int kyear
            sum34 += firstPresent(in, "NR" + year);
            if firstPresent(in, "NR" + year)!=0 {
                kyear++;
            }
        }
        double b34raw = sum34 / kYears;
        out.put("B34_raw", b34raw);
        out.put("B34", Normalizer.clamp01(b34raw, 0.3, 1.5) * 2.0);

        // B41 — публикации на 100 НПР
        // B41_raw = (Σ (WL[Y] / NPR[Y]), Y=2022..2022+k-1) * 100 / k
        double sum41 = 0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double wl = firstPresent(in, "WL" + year);
            double npr = firstPresent(in, "NPR" + year);
            if (npr > 0) {
                sum41 += wl / npr;
            }
        }
        double b41raw = (sum41 * 100.0) / kYears;
        out.put("B41_raw", b41raw);
        out.put("B41", Normalizer.clamp01(b41raw, 5.0, 100.0) * 5.0);

        // B42 — доходы от НИОКР на 1 НПР
        // B42_raw = (Σ (DN[Y] / NPR[Y]), Y=2022..2022+k-1) / k
        double sum42 = 0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double dn = firstPresent(in, "DN" + year);
            double npr = firstPresent(in, "NPR" + year);
            if (npr > 0) {
                sum42 += dn / npr;
            }
        }
        double b42raw = sum42 / kYears;
        out.put("B42_raw", b42raw);
        out.put("B42", Normalizer.clamp01(b42raw, 100.0, 1000.0) * 5.0);

        // B43 — доля иностранных обучающихся
        double io = v.apply("Io");
        double iv = v.apply("Iv");
        double iz = v.apply("Iz");
        double no = v.apply("No");
        double nv = v.apply("Nv");
        double nz = v.apply("Nz");
        double denom43 = no + 0.25 * nv + 0.1 * nz;
        double b43raw = denom43 == 0 ? 0.0 : (io + 0.25 * iv + 0.1 * iz) / denom43 * 100.0;
        out.put("B43_raw", b43raw);
        out.put("B43", Normalizer.clamp01(b43raw, 1.0, 15.0) * 5.0);

        // B44 — доходы на 1 обучающегося
        // where PN_k = 1.0*NO_k + 0.25*NV_k + 0.1*NZ_k + NOA_k
        // B44_raw = (Σ (OD[Y] / PN[Y]), Y=2022..2022+k-1) / k
        double sum44 = 0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double od = firstPresent(in, "OD" + year);
            double noYear = firstPresent(in, "NO" + year, "No" + year);
            double nvYear = firstPresent(in, "NV" + year, "Nv" + year);
            double nzYear = firstPresent(in, "NZ" + year, "Nz" + year);
            double noaYear = firstPresent(in, "NOA" + year, "Noa" + year);
            double pnYear = 1.0 * noYear + 0.25 * nvYear + 0.1 * nzYear + noaYear;
            if (pnYear <= 0) {
                pnYear = firstPresent(in, "PN" + year);
            }
            if (pnYear > 0) {
                sum44 += od / pnYear;
            }
        }
        double b44raw = sum44 / kYears;
        out.put("B44_raw", b44raw);
        out.put("B44", Normalizer.clamp01(b44raw, 50.0, 500.0) * 5.0);

        return new DocumentCalcDto(out);
    }

    private static double firstPresent(DocumentParamsDto in, String... keys) {
        for (String key : keys) {
            if (in.values().containsKey(key)) {
                Double value = in.values().get(key);
                return value == null ? 0.0 : value;
            }
        }
        return 0.0;
    }
}
