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
        double pno = firstPresent(in, "PNo", "No");
        double pnv = firstPresent(in, "PNv", "Nv");
        double pnz = firstPresent(in, "PNz", "Nz");
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
        if (hasAnyKey(in, "KI", "KI_raw")) {
            ki = firstPresent(in, "KI", "KI_raw");
        }
        out.put("KI", ki);

        double kiA = hasAnyKey(in, "A_KI", "KI_A") ? firstPresent(in, "A_KI", "KI_A") : ki;
        double kiB = hasAnyKey(in, "B_KI", "KI_B") ? firstPresent(in, "B_KI", "KI_B") : ki;
        double kiM = hasAnyKey(in, "M_KI", "KI_M") ? firstPresent(in, "M_KI", "KI_M") : ki;
        out.put("KI_A", kiA);
        out.put("KI_B", kiB);
        out.put("KI_M", kiM);

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
        double b12 = Normalizer.clamp01(b12raw, 80.0, 100.0)* 3.0;
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
        double nbp = in.values().containsKey("NBP")
            ? v.apply("NBP")
            : 1.0 * v.apply("NBo") + 0.25 * v.apply("NBv") + 0.1 * v.apply("NBz");
        double nmp = in.values().containsKey("NMP")
            ? v.apply("NMP")
            : 1.0 * v.apply("NMo") + 0.25 * v.apply("NMv") + 0.1 * v.apply("NMz");
        double acp = in.values().containsKey("ACP")
            ? v.apply("ACP")
            : 1.0 * v.apply("ACo") + 0.25 * v.apply("ACv") + 0.1 * v.apply("ACz");
        double opc = v.apply("OPC");
        double acc = v.apply("ACC");
        double b22raw = Normalizer.safeDiv(nmp + 3.0 * (acp + opc + acc), nbp);
        out.put("B22_raw", b22raw);
        double b22 = Normalizer.clamp01(b22raw, 0.0, 0.25) * 6.0;
        out.put("B22", b22);

        // B23 = (0.25*PKP + PPP) / (NP + NOA)
        // where:
        // NP  = 1.0*NPo + 0.25*NPv + 0.1*NPz
        // PKP = 1.0*KPo + 0.25*KPv + 0.1*KPz
        // PPP = 1.0*PPPo + 0.25*PPPv + 0.1*PPPz
        double pkp = 1.0 * v.apply("KPo") + 0.25 * v.apply("KPv") + 0.1 * v.apply("KPz");
        double ppp = 1.0 * v.apply("PPPo") + 0.25 * v.apply("PPPv") + 0.1 * v.apply("PPPz");
        // backward compatibility: if NPo/NPv/NPz are missing, fallback to legacy No/Nv/Nz
        double npo = firstPresent(in, "NPo", "No");
        double npv = firstPresent(in, "NPv", "Nv");
        double npz = firstPresent(in, "NPz", "Nz");
        double np = 1.0 * npo + 0.25 * npv + 0.1 * npz;
        double noa = v.apply("NOA");
        double b23Numerator = 0.25 * pkp + ppp;
        double b23Denominator = np + noa;
        double b23raw;
        if (b23Denominator == 0.0 && b23Numerator > 0.0) {
            b23raw = 1.0;
        } else {
            b23raw = Normalizer.safeDiv(b23Numerator, b23Denominator);
        }
        out.put("B23_raw", b23raw);
        double b23 = Normalizer.clamp01(b23raw, 0.0, 0.20) * 6.0;
        out.put("B23", b23);

        // B24 = NAP / PN
        // where:
        // NAP = 1.0*NAo + 0.25*NAv + 0.1*NAz
        // P   = 1.0*Po + 0.25*Pv + 0.1*Pz
        // legacy fallback: PNo/PNv/PNz
        double nap = in.values().containsKey("NAP")
            ? v.apply("NAP")
            : 1.0 * v.apply("NAo") + 0.25 * v.apply("NAv") + 0.1 * v.apply("NAz");
        double po = firstPresent(in, "Po", "PNo", "No");
        double pv = firstPresent(in, "Pv", "PNv", "Nv");
        double pz = firstPresent(in, "Pz", "PNz", "Nz");
        double p = 1.0 * po + 0.25 * pv + 0.1 * pz;
        out.put("P_raw", p);
        double pForB24;
        if (in.values().containsKey("P")) {
            pForB24 = v.apply("P");
        } else if (hasAnyKey(in, "Po", "Pv", "Pz", "PNo", "PNv", "PNz", "No", "Nv", "Nz")) {
            pForB24 = p;
        } else if (in.values().containsKey("PN")) {
            pForB24 = v.apply("PN");
        } else {
            pForB24 = 0.0;
        }
        double b24raw = Normalizer.safeDiv(nap, pForB24);
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
            double b33 = b33raw;
            out.put("B33", b33);
        }

        // --------------------------------------------------
        // B34 — востребованность на рынке труда
        // B34_raw = (Σ NR[Y], Y=2023..2023+k-1) / k34,
        // где k34 — количество лет с ненулевым NR[Y].
        double sum34 = 0.0;
        int k34 = 0;
        for (int i = 0; i < kYears; i++) {
            int year = 2023 + i;
            double nr = firstPresent(in, "NR" + year);
            sum34 += nr;
            if (nr != 0.0) {
                k34++;
            }
        }
        double b34raw = k34 > 0 ? (sum34 / k34) : 0.0;
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

        double bTotal = out.getOrDefault("B11", 0.0)
            + out.getOrDefault("B12", 0.0)
            + out.getOrDefault("B13", 0.0)
            + out.getOrDefault("B21", 0.0)
            + out.getOrDefault("B22", 0.0)
            + out.getOrDefault("B23", 0.0)
            + out.getOrDefault("B24", 0.0)
            + out.getOrDefault("B25", 0.0)
            + out.getOrDefault("B26", 0.0)
            + out.getOrDefault("B31", 0.0)
            + out.getOrDefault("B32", 0.0)
            + out.getOrDefault("B33", 0.0)
            + out.getOrDefault("B34", 0.0)
            + out.getOrDefault("B41", 0.0)
            + out.getOrDefault("B42", 0.0)
            + out.getOrDefault("B43", 0.0)
            + out.getOrDefault("B44", 0.0);
        double bTotalWithKi = bTotal * kiB;
        out.put("B_TOTAL", bTotal);
        out.put("B_TOTAL_WITH_KI", bTotalWithKi);

        // -----------------------
        // 3. категория A (A31..A33)
        // -----------------------

        // A31_raw = (Σ (WL[Y] / NPR[Y]), Y=2022..2022+k-1) * 100 / k
        double a31raw = b41raw;
        out.put("A31_raw", a31raw);
        out.put("A31", Normalizer.clamp01(a31raw, 5.0, 100.0) * 8.0);

        // A32_raw = (Σ (DN[Y] / NPR[Y]), Y=2022..2022+k-1) / k
        double a32raw = b42raw;
        out.put("A32_raw", a32raw);
        out.put("A32", Normalizer.clamp01(a32raw, 100.0, 1000.0) * 8.0);

        // A33_raw = (Σ (RDN[Y] / NPR[Y]), Y=2022..2022+k-1) / k
        double sumA33 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double rdn = firstPresent(in, "RDN" + year, "РДН" + year);
            double npr = firstPresent(in, "NPR" + year);
            if (npr > 0) {
                sumA33 += rdn / npr;
            }
        }
        double a33raw = sumA33 / kYears;
        out.put("A33_raw", a33raw);
        out.put("A33", Normalizer.clamp01(a33raw, 50.0, 500.0) * 4.0);

        // A11 = (PRF / KCO) * 100
        double a11raw = Normalizer.safeDiv(
            firstPresent(in, "PRF", "A11_PRF"),
            firstPresent(in, "KCO", "KTSO", "КЦО", "A11_KCO")
        ) * 100.0;
        out.put("A11_raw", a11raw);
        out.put("A11", Normalizer.clamp01(a11raw, 80.0, 100.0) * 5.0);

        // A21 = (ZKN / CHVA) * 100
        double a21raw = Normalizer.safeDiv(
            firstPresent(in, "ZKN", "ЗКН", "A21_ZKN"),
            firstPresent(in, "CHVA", "ЧВА", "A21_CHVA")
        ) * 100.0;
        out.put("A21_raw", a21raw);
        out.put("A21", Normalizer.clamp01(a21raw, 20.0, 80.0) * 25.0);

        // A22 = (ZKN / CHPA) * 100
        double a22raw = Normalizer.safeDiv(
            firstPresent(in, "ZKN", "ЗКН", "A22_ZKN"),
            firstPresent(in, "CHPA", "ЧПА", "A22_CHPA")
        ) * 100.0;
        out.put("A22_raw", a22raw);
        out.put("A22", Normalizer.clamp01(a22raw, 10.0, 50.0) * 25.0);

        // A23 = (CZ / CV) * 100, if CV is absent or zero use A23RF fallback
        double cv = firstPresent(in, "CV", "ЦВ", "A23_CV");
        double a23raw;
        if (cv > 0.0) {
            a23raw = Normalizer.safeDiv(
                firstPresent(in, "CZ", "ЦЗ", "A23_CZ"),
                cv
            ) * 100.0;
            out.put("A23_raw", a23raw);
            out.put("A23", Normalizer.clamp01(a23raw, 30.0, 90.0) * 1.0);
        } else {
            a23raw = firstPresent(in, "A23RF", "A23_avg", "A23_RF");
            out.put("A23_raw", a23raw);
            out.put("A23", Normalizer.clamp01(a23raw, 0.0, 1.0) * 1.0);
        }

        // A34 = average((IA[Y]/ASP[Y]) * 100), Y=2022..2022+k-1
        double sumA34 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double iaYear = firstPresent(in, "IA" + year, "ИА" + year);
            double aspYear = firstPresent(in, "ASP" + year, "АСП" + year);
            if (aspYear > 0.0) {
            sumA34 += (iaYear / aspYear) * 100.0;
            }
        }
        double a34raw = sumA34 / kYears;
        out.put("A34_raw", a34raw);
        out.put("A34", Normalizer.clamp01(a34raw, 1.0, 15.0) * 4.0);

        // A35 = average(OD[Y] / NPR[Y]), Y=2022..2022+k-1
        double sumA35 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double odYear = firstPresent(in, "OD" + year);
            double nprYear = firstPresent(in, "NPR" + year);
            if (nprYear > 0.0) {
            sumA35 += odYear / nprYear;
            }
        }
        double a35raw = sumA35 / kYears;
        out.put("A35_raw", a35raw);
        out.put("A35", Normalizer.clamp01(a35raw, 1000.0, 5000.0) * 8.0);

        // A36 = PFN / ASO (ASO minimum is 3)
        double aso = firstPresent(in, "ASO", "АСО", "A36_ASO");
        double normalizedAso = aso < 3.0 ? 3.0 : aso;
        double a36raw = Normalizer.safeDiv(
            firstPresent(in, "PFN", "ПФН", "A36_PFN"),
            normalizedAso
        );
        out.put("A36_raw", a36raw);
        out.put("A36", Normalizer.clamp01(a36raw, 300.0, 3000.0) * 8.0);

        // A37 is an indicator in [0, 1]
        double a37raw = firstPresent(in, "A37_o", "A37_raw", "DS", "A37");
        out.put("A37_raw", a37raw);
        out.put("A37", Normalizer.clamp01(a37raw, 0.0, 1.0) * 2.0);

        double aTotal = out.get("A11") + out.get("A21") + out.get("A22") + out.get("A23")
            + out.get("A31") + out.get("A32") + out.get("A33")
            + out.get("A34") + out.get("A35") + out.get("A36") + out.get("A37");
        double aTotalWithKi = aTotal * kiA;
        out.put("A_TOTAL", aTotal);
        out.put("A_TOTAL_WITH_KI", aTotalWithKi);

        // -----------------------
        // группа M (M11..M27, M31..M33, M41..M44)
        // -----------------------

        // M11 = (ZMD / ZM) * 100
        double m11raw = Normalizer.safeDiv(
            firstPresent(in, "ZMD", "ЗМД", "M11_ZMD"),
            firstPresent(in, "ZM", "ЗМ", "M11_ZM")
        ) * 100.0;
        out.put("M11_raw", m11raw);
        out.put("M11", Normalizer.clamp01(m11raw, 10.0, 50.0) * 10.0);

        // M12 = CHZ / ZPK
        double m12raw = Normalizer.safeDiv(
            firstPresent(in, "CHZ", "ЧЗ", "M12_CHZ"),
            firstPresent(in, "ZPK", "ЗПК", "M12_ZPK")
        );
        out.put("M12_raw", m12raw);
        out.put("M12", Normalizer.clamp01(m12raw, 1.5, 4.0) * 5.0);

        // M13 = MDP / ZPK
        double m13raw = Normalizer.safeDiv(
            firstPresent(in, "MDP", "МДП", "M13_MDP"),
            firstPresent(in, "ZPK", "ЗПК", "M13_ZPK")
        );
        out.put("M13_raw", m13raw);
        out.put("M13", Normalizer.clamp01(m13raw, 0.0, 0.25) * 5.0);

        // M14 = (PRF / KCO) * 100
        double m14raw = Normalizer.safeDiv(
            firstPresent(in, "PRF", "M14_PRF"),
            firstPresent(in, "KCO", "KTSO", "КЦО", "M14_KCO")
        ) * 100.0;
        out.put("M14_raw", m14raw);
        out.put("M14", Normalizer.clamp01(m14raw, 80.0, 100.0) * 5.0);

        // M21 is an indicator in [0, 1]
        double m21raw = firstPresent(in, "M21_o", "M21_raw", "M21");
        out.put("M21_raw", m21raw);
        out.put("M21", Normalizer.clamp01(m21raw, 0.0, 1.0) * 2.0);

        // M22 corresponds to B22 ratio and normalization
        double m22raw = in.values().containsKey("M22_o") ? v.apply("M22_o") : b22raw;
        out.put("M22_raw", m22raw);
        out.put("M22", Normalizer.clamp01(m22raw, 0.0, 0.25) * 6.0);

        // M23 corresponds to B23 ratio and normalization
        double m23raw = in.values().containsKey("M23_o") ? v.apply("M23_o") : b23raw;
        out.put("M23_raw", m23raw);
        out.put("M23", Normalizer.clamp01(m23raw, 0.0, 0.20) * 6.0);

        // M24 = NAP / PN
        // Allows either pre-aggregated raw value or explicit NAP/PN inputs.
        double m24raw;
        if (in.values().containsKey("M24_o")) {
            m24raw = v.apply("M24_o");
        } else {
            double m24Nap = firstPresent(in, "NAP", "M24_NAP", "NAo");
            double m24Pn = firstPresent(in, "PN", "M24_PN", "NMP");
            m24raw = Normalizer.safeDiv(m24Nap, m24Pn);
        }
        out.put("M24_raw", m24raw);
        out.put("M24", Normalizer.clamp01(m24raw, 0.0, 0.5) * 6.0);

        // M31_o может приходить уже агрегированным из внешнего расчёта.
        double m31raw = firstPresent(in, "M31_o", "M31_raw", "M31raw");
        out.put("M31_raw", m31raw);
        out.put("M31", Normalizer.clamp01(m31raw, 1.0, 5.0) * 20.0);

        // M32 uses only M-row inputs and is independent from B block values.
        double m32raw = Normalizer.safeDiv(
            firstPresent(in, "M_N", "N_M", "N")
                + firstPresent(in, "M_VO", "VO_M", "VO")
                - firstPresent(in, "M_PO", "PO_M", "PO"),
            firstPresent(in, "M_Npr", "Npr_M", "Npr")
        ) * 100.0;
        out.put("M32_raw", m32raw);
        out.put("M32", Normalizer.clamp01(m32raw, 75.0, 90.0) * 5.0);

        // M33 uses only M-row labor-market inputs and is independent from B block values.
        double sumM33 = 0.0;
        int kM33 = 0;
        for (int i = 0; i < kYears; i++) {
            int year = 2023 + i;
            double nr = firstPresent(in, "M_NR" + year, "M33_NR" + year, "NR" + year);
            sumM33 += nr;
            if (nr != 0.0) {
                kM33++;
            }
        }
        double m33raw = kM33 > 0 ? (sumM33 / kM33) : 0.0;
        out.put("M33_raw", m33raw);
        out.put("M33", Normalizer.clamp01(m33raw, 0.3, 1.5) * 2.0);

        // M25 = (BP + CP) / NMP
        // BP = 1.0*BPo + 0.25*BPv + 0.1*BPz
        // CP = 1.0*CPo + 0.25*CPv + 0.1*CPz
        // NMP = 1.0*NMo + 0.25*NMv + 0.1*NMz
        double m25raw;
        if (in.values().containsKey("M25_o")) {
            m25raw = v.apply("M25_o");
        } else {
            double bp = 1.0 * firstPresent(in, "M25_BPo", "BPo")
                + 0.25 * firstPresent(in, "M25_BPv", "BPv")
                + 0.1 * firstPresent(in, "M25_BPz", "BPz");
            double cp = 1.0 * firstPresent(in, "M25_CPo", "CPo")
                + 0.25 * firstPresent(in, "M25_CPv", "CPv")
                + 0.1 * firstPresent(in, "M25_CPz", "CPz");
            double mNmp = 1.0 * firstPresent(in, "M25_NMo", "NMo")
                + 0.25 * firstPresent(in, "M25_NMv", "NMv")
                + 0.1 * firstPresent(in, "M25_NMz", "NMz");
            if ((bp + cp) > 0.0 && mNmp == 0.0) {
                m25raw = 1.0;
            } else {
                m25raw = Normalizer.safeDiv(bp + cp, mNmp);
            }
        }
        out.put("M25_raw", m25raw);
        out.put("M25", Normalizer.clamp01(m25raw, 0.0, 15.0) * 1.0);

        // M26 = average((CHPSi/CHPi) * 100), i=2022..2022+k-1
        double sumM26 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double chps = firstPresent(in,
                "M26_CHPSi" + year,
                "M_CHPSi" + year,
                "CHPSi" + year,
                "ЧПСi" + year,
                "CPSi" + year);
            double chp = firstPresent(in,
                "M26_CHPi" + year,
                "M_CHPi" + year,
                "CHPi" + year,
                "ЧПi" + year,
                "CPi" + year);
            if (chp > 0) {
                sumM26 += chps / chp;
            }
        }
        double m26raw = (sumM26 * 100.0) / kYears;
        out.put("M26_raw", m26raw);
        out.put("M26", Normalizer.clamp01(m26raw, 0.0, 25.0) * 1.0);

        // M27 = average((CHOSi/CHOi) * 100), i=2022..2022+k-1
        double sumM27 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double chos = firstPresent(in,
                "M27_CHOSi" + year,
                "M_CHOSi" + year,
                "CHOSi" + year,
                "ЧОСi" + year,
                "COSi" + year);
            double cho = firstPresent(in,
                "M27_CHOi" + year,
                "M_CHOi" + year,
                "CHOi" + year,
                "ЧОi" + year,
                "COi" + year);
            if (cho > 0) {
                sumM27 += chos / cho;
            }
        }
        double m27raw = (sumM27 * 100.0) / kYears;
        out.put("M27_raw", m27raw);
        out.put("M27", Normalizer.clamp01(m27raw, 0.0, 30.0) * 1.0);

        // M41 — publications per 100 NPR
        double sumM41 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double wl = firstPresent(in, "M_WL" + year, "M41_WL" + year, "WL" + year);
            double npr = firstPresent(in, "M_NPR" + year, "M41_NPR" + year, "NPR" + year);
            if (npr > 0) {
                sumM41 += wl / npr;
            }
        }
        double m41raw = (sumM41 * 100.0) / kYears;
        out.put("M41_raw", m41raw);
        out.put("M41", Normalizer.clamp01(m41raw, 5.0, 100.0) * 8.0);

        // M42 — R&D income per NPR
        double sumM42 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double dn = firstPresent(in, "M_DN" + year, "M42_DN" + year, "DN" + year);
            double npr = firstPresent(in, "M_NPR" + year, "M42_NPR" + year, "NPR" + year);
            if (npr > 0) {
                sumM42 += dn / npr;
            }
        }
        double m42raw = sumM42 / kYears;
        out.put("M42_raw", m42raw);
        out.put("M42", Normalizer.clamp01(m42raw, 100.0, 1000.0) * 8.0);

        // M43 — foreign students share
        double mIo = firstPresent(in, "M_Io", "M43_Io", "Io");
        double mIv = firstPresent(in, "M_Iv", "M43_Iv", "Iv");
        double mIz = firstPresent(in, "M_Iz", "M43_Iz", "Iz");
        double mNo = firstPresent(in, "M_No", "M43_No", "No");
        double mNv = firstPresent(in, "M_Nv", "M43_Nv", "Nv");
        double mNz = firstPresent(in, "M_Nz", "M43_Nz", "Nz");
        double m43den = mNo + 0.25 * mNv + 0.1 * mNz;
        double m43raw = m43den == 0 ? 0.0 : (mIo + 0.25 * mIv + 0.1 * mIz) / m43den * 100.0;
        out.put("M43_raw", m43raw);
        out.put("M43", Normalizer.clamp01(m43raw, 1.0, 15.0) * 7.0);

        // M44 — total income per weighted student body
        double sumM44 = 0.0;
        for (int i = 0; i < kYears; i++) {
            int year = 2022 + i;
            double od = firstPresent(in, "M_OD" + year, "M44_OD" + year, "OD" + year);
            double noYear = firstPresent(in, "M_NO" + year, "M44_NO" + year, "NO" + year, "No" + year);
            double nvYear = firstPresent(in, "M_NV" + year, "M44_NV" + year, "NV" + year, "Nv" + year);
            double nzYear = firstPresent(in, "M_NZ" + year, "M44_NZ" + year, "NZ" + year, "Nz" + year);
            double noaYear = firstPresent(in, "M_NOA" + year, "M44_NOA" + year, "NOA" + year, "Noa" + year);
            double pnYear = 1.0 * noYear + 0.25 * nvYear + 0.1 * nzYear + noaYear;
            if (pnYear <= 0) {
                pnYear = firstPresent(in, "M_PN" + year, "PN" + year);
            }
            if (pnYear > 0) {
                sumM44 += od / pnYear;
            }
        }
        double m44raw = sumM44 / kYears;
        out.put("M44_raw", m44raw);
        out.put("M44", Normalizer.clamp01(m44raw, 50.0, 500.0) * 7.0);

        double mTotal = out.get("M11") + out.get("M12") + out.get("M13") + out.get("M14")
            + out.get("M21") + out.get("M22") + out.get("M23") + out.get("M24")
            + out.get("M25") + out.get("M26") + out.get("M27")
            + out.get("M31") + out.get("M32") + out.get("M33")
            + out.get("M41") + out.get("M42") + out.get("M43") + out.get("M44");
        double mTotalWithKi = mTotal * kiM;
        out.put("M_TOTAL", mTotal);
        out.put("M_TOTAL_WITH_KI", mTotalWithKi);

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

    private static boolean hasAnyKey(DocumentParamsDto in, String... keys) {
        for (String key : keys) {
            if (in.values().containsKey(key)) {
                return true;
            }
        }
        return false;
    }
}
