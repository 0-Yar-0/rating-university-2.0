package ru.ystu.rating.university.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.ystu.rating.university.dto.DocumentCalcDto;
import ru.ystu.rating.university.dto.DocumentParamsDto;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DocumentServiceTest {
    private DocumentService svc;

    @BeforeEach
    void setUp() {
        svc = new DocumentService();
    }

    @Test
    void testPnDiKiAndOverall() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("PNo", 907.0);
        inputs.put("PNv", 0.0);
        inputs.put("PNz", 345.0);
        inputs.put("DIo", 19.0);
        inputs.put("DIv", 0.0);
        inputs.put("DIz", 3.0);
        inputs.put("sumPoints", 57.771);
        // extra parameters for B34..B44
        inputs.put("NR2023", 0.0);
        inputs.put("NR2024", 0.0);
        inputs.put("NR2025", 0.95);
        inputs.put("WL2022", 60.0);
        inputs.put("WL2023", 70.0);
        inputs.put("WL2024", 70.0);
        inputs.put("NPR2022", 270.1);
        inputs.put("NPR2023", 278.9);
        inputs.put("NPR2024", 278.5);
        inputs.put("DN2022", 38567.4);
        inputs.put("DN2023", 96735.9);
        inputs.put("DN2024", 483281.1);
        inputs.put("Io", 47.0);
        inputs.put("Iv", 8.0);
        inputs.put("Iz", 10.0);
        inputs.put("No", 3296.0);
        inputs.put("Nv", 240.0);
        inputs.put("Nz", 1412.0);
        inputs.put("OD2022", 760316.0);
        inputs.put("OD2023", 870778.8);
        inputs.put("OD2024", 1348285.7);
        inputs.put("PN2022", 3520.7);
        inputs.put("PN2023", 3569.9);
        inputs.put("PN2024", 3497.2);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));
        assertEquals(941.5, out.get("PN"), 1e-6);
        assertEquals(19.3, out.get("DI"), 1e-6);
        assertEquals(1.0, out.get("KI"), 1e-6);
        assertEquals(57.771, out.get("TOTAL"), 1e-6);
        // verify additional formulas produce weighted scores (norm * weight)
        assertEquals(1.083, out.get("B34"), 1e-3);
        assertEquals(1.008, out.get("B41"), 1e-3);
        assertEquals(3.565, out.get("B42"), 1e-3);
        assertEquals(0.153, out.get("B43"), 1e-3);
        assertEquals(2.576, out.get("B44"), 1e-3);
        // A-category (3rd group) mirrors raw components but has its own weights
        assertEquals(24.149, out.get("A31_raw"), 1e-3);
        assertEquals(1.613, out.get("A31"), 1e-3);
        assertEquals(741.646, out.get("A32_raw"), 1e-3);
        assertEquals(5.703, out.get("A32"), 1e-3);
        // M33 uses the same raw basis as B34 in this implementation
        assertEquals(out.get("B34"), out.get("M33"), 1e-9);
        assertEquals(out.get("B_TOTAL") * out.get("KI"), out.get("B_TOTAL_WITH_KI"), 1e-9);
    }

    @Test
    void testA33Computation() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("RDN2022", 24155.0);
        inputs.put("RDN2023", 83435.9);
        inputs.put("RDN2024", 460306.2);
        inputs.put("NPR2022", 270.1);
        inputs.put("NPR2023", 278.9);
        inputs.put("NPR2024", 278.5);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(680.5, out.get("A33_raw"), 1e-1);
        assertEquals(4.0, out.get("A33"), 1e-6);
    }

    @Test
    void testB22Computation() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("NBo", 907.0);
        inputs.put("NBv", 0.0);
        inputs.put("NBz", 345.0);
        inputs.put("NMo", 79.0);
        inputs.put("NMv", 0.0);
        inputs.put("NMz", 3.0);
        inputs.put("ACo", 11.8);
        inputs.put("ACv", 0.0);
        inputs.put("ACz", 0.0);
        inputs.put("OPC", 0.0);
        inputs.put("ACC", 0.0);
        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));
        // NBP = 941.5, NMP = 79.3, ACP = 11.8 => raw 0.121826...
        assertEquals(0.12183, out.get("B22_raw"), 1e-4);
        assertEquals(2.924, out.get("B22"), 1e-3);
        // other metrics already validated in first test
    }

    @Test
    void testB22ComputationFromAggregatedInputs() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("NBP", 941.5);
        inputs.put("NMP", 79.3);
        inputs.put("ACP", 11.8);
        inputs.put("OPC", 0.0);
        inputs.put("ACC", 0.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(0.12183, out.get("B22_raw"), 1e-4);
        assertEquals(2.924, out.get("B22"), 1e-3);
    }

    @Test
    void testB24ComputationWithAggregatedNapAndPnFallback() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("NAP", 100.0);
        // no PNo/PNv/PNz -> PN should fallback to No/Nv/Nz
        inputs.put("No", 200.0);
        inputs.put("Nv", 0.0);
        inputs.put("Nz", 0.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(200.0, out.get("PN"), 1e-6);
        assertEquals(0.5, out.get("B24_raw"), 1e-6);
        assertEquals(6.0, out.get("B24"), 1e-6);
    }

    @Test
    void testB24UsesDedicatedPoPvPzInsteadOfCorrectionPn() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("NAP", 100.0);
        inputs.put("PNo", 400.0);
        inputs.put("PNv", 0.0);
        inputs.put("PNz", 0.0);
        inputs.put("Po", 200.0);
        inputs.put("Pv", 0.0);
        inputs.put("Pz", 0.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(400.0, out.get("PN"), 1e-6);
        assertEquals(0.5, out.get("B24_raw"), 1e-6);
        assertEquals(6.0, out.get("B24"), 1e-6);
    }

    @Test
    void testB23UsesSpecialRuleWhenDenominatorIsZero() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("KPo", 10.0);
        inputs.put("KPv", 0.0);
        inputs.put("KPz", 0.0);
        inputs.put("PPPo", 0.0);
        inputs.put("PPPv", 0.0);
        inputs.put("PPPz", 0.0);
        inputs.put("NPo", 0.0);
        inputs.put("NPv", 0.0);
        inputs.put("NPz", 0.0);
        inputs.put("NOA", 0.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(1.0, out.get("B23_raw"), 1e-6);
        assertEquals(6.0, out.get("B23"), 1e-6);
        assertEquals(6.0, out.get("M23"), 1e-6);
    }

    @Test
    void testB25B26WithDynamicK() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("k", 2.0);

        inputs.put("CHPSi2022", 10.0);
        inputs.put("CHPi2022", 100.0);
        inputs.put("CHPSi2023", 20.0);
        inputs.put("CHPi2023", 100.0);

        inputs.put("CHOSi2022", 5.0);
        inputs.put("CHOi2022", 100.0);
        inputs.put("CHOSi2023", 15.0);
        inputs.put("CHOi2023", 100.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        // B25_raw = ((0.1 + 0.2) * 100) / 2 = 15
        assertEquals(15.0, out.get("B25_raw"), 1e-6);
        assertEquals(0.375, out.get("B25"), 1e-6);

        // B26_raw = ((0.05 + 0.15) * 100) / 2 = 10
        assertEquals(10.0, out.get("B26_raw"), 1e-6);
        assertEquals(0.25, out.get("B26"), 1e-6);
    }

    @Test
    void testB31AndB32() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("UT", 147.0);
        inputs.put("DO", 197.0);
        inputs.put("N", 1250.0);
        inputs.put("Npr", 1195.0);
        inputs.put("VO", 200.0);
        inputs.put("PO", 362.0);
        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));
        assertEquals(74.62, out.get("B31_raw"), 1e-2);
        assertEquals(10.546, out.get("B31"), 1e-3);
        assertEquals(91.05, out.get("B32_raw"), 1e-2);
        assertEquals(5.000, out.get("B32"), 1e-3);
        // M32 is aligned with B32 normalization and weight
        assertEquals(out.get("B32_raw"), out.get("M32_raw"), 1e-9);
        assertEquals(out.get("B32"), out.get("M32"), 1e-9);
    }

    @Test
    void testExtendedAMetricsAndATotal() {
        Map<String, Double> inputs = new HashMap<>();

        // A11
        inputs.put("PRF", 74.0);
        inputs.put("KCO", 74.0);

        // A21
        inputs.put("ZKN", 2.0);
        inputs.put("CHVA", 2.0);

        // A22
        inputs.put("CHPA", 4.0);

        // A23 (fallback branch)
        inputs.put("CV", 0.0);
        inputs.put("A23RF", 0.56);

        // A31
        inputs.put("WL2022", 60.0);
        inputs.put("WL2023", 70.0);
        inputs.put("WL2024", 70.0);
        inputs.put("NPR2022", 270.1);
        inputs.put("NPR2023", 278.9);
        inputs.put("NPR2024", 278.5);

        // A32
        inputs.put("DN2022", 38567.4);
        inputs.put("DN2023", 96735.9);
        inputs.put("DN2024", 483281.1);

        // A33
        inputs.put("RDN2022", 24155.0);
        inputs.put("RDN2023", 83435.9);
        inputs.put("RDN2024", 460306.2);

        // A34
        inputs.put("IA2022", 0.0);
        inputs.put("IA2023", 0.0);
        inputs.put("IA2024", 0.0);
        inputs.put("ASP2022", 28.0);
        inputs.put("ASP2023", 48.0);
        inputs.put("ASP2024", 99.0);

        // A35
        inputs.put("OD2022", 810300.0);
        inputs.put("OD2023", 836700.0);
        inputs.put("OD2024", 835500.0);

        // A36/A37
        inputs.put("PFN", 900.0);
        inputs.put("ASO", 3.0);
        inputs.put("A37_o", 1.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(5.0, out.get("A11"), 1e-6);
        assertEquals(25.0, out.get("A21"), 1e-6);
        assertEquals(25.0, out.get("A22"), 1e-6);
        assertEquals(0.56, out.get("A23"), 1e-6);
        assertEquals(1.613, out.get("A31"), 1e-3);
        assertEquals(5.703, out.get("A32"), 1e-3);
        assertEquals(4.0, out.get("A33"), 1e-6);
        assertEquals(0.0, out.get("A34"), 1e-6);
        assertEquals(4.0, out.get("A35"), 1e-6);
        assertEquals(0.0, out.get("A36"), 1e-6);
        assertEquals(2.0, out.get("A37"), 1e-6);
        assertEquals(72.876, out.get("A_TOTAL"), 1e-3);
        assertEquals(out.get("A_TOTAL") * out.get("KI"), out.get("A_TOTAL_WITH_KI"), 1e-9);
    }

    @Test
    void testExtendedMMetricsAndMTotal() {
        Map<String, Double> inputs = new HashMap<>();

        // M11
        inputs.put("ZMD", 30.0);
        inputs.put("ZM", 100.0);

        // M12/M13
        inputs.put("CHZ", 275.0);
        inputs.put("ZPK", 100.0);
        inputs.put("MDP", 12.5);

        // M14
        inputs.put("PRF", 74.0);
        inputs.put("KCO", 74.0);

        // M21/M22/M23
        inputs.put("M21_o", 1.0);
        inputs.put("M22_o", 0.125);
        inputs.put("M23_o", 0.1);

        // M31
        inputs.put("M31_o", 3.0);

        // M32 via B32 basis
        inputs.put("N", 80.0);
        inputs.put("VO", 5.0);
        inputs.put("PO", 2.5);
        inputs.put("Npr", 100.0);

        // M33 via B34 basis
        inputs.put("NR2023", 0.9);
        inputs.put("NR2024", 0.9);
        inputs.put("NR2025", 0.9);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(5.0, out.get("M11"), 1e-6);
        assertEquals(2.5, out.get("M12"), 1e-6);
        assertEquals(2.5, out.get("M13"), 1e-6);
        assertEquals(5.0, out.get("M14"), 1e-6);
        assertEquals(2.0, out.get("M21"), 1e-6);
        assertEquals(3.0, out.get("M22"), 1e-6);
        assertEquals(3.0, out.get("M23"), 1e-6);
        assertEquals(10.0, out.get("M31"), 1e-6);
        assertEquals(2.5, out.get("M32"), 1e-6);
        assertEquals(1.0, out.get("M33"), 1e-6);
        assertEquals(36.5, out.get("M_TOTAL"), 1e-6);
        assertEquals(out.get("M_TOTAL") * out.get("KI"), out.get("M_TOTAL_WITH_KI"), 1e-9);
    }

    @Test
    void testSeparateSubmetricsAndKiOverrides() {
        Map<String, Double> inputs = new HashMap<>();

        // global KI override
        inputs.put("KI", 1.2);

        // class-specific KI overrides
        inputs.put("A_KI", 1.1);
        inputs.put("B_KI", 1.3);
        inputs.put("M_KI", 1.4);

        // totals are passed but must NOT override computed totals anymore
        inputs.put("A_TOTAL", 100.0);
        inputs.put("B_TOTAL", 200.0);
        inputs.put("M_TOTAL", 300.0);

        // A submetrics
        inputs.put("A11", 5.0);
        inputs.put("A21", 10.0);
        inputs.put("A22", 15.0);
        inputs.put("A23", 20.0);
        inputs.put("A31", 1.0);
        inputs.put("A32", 2.0);
        inputs.put("A33", 3.0);
        inputs.put("A34", 4.0);
        inputs.put("A35", 5.0);
        inputs.put("A36", 6.0);
        inputs.put("A37", 7.0);

        // B submetrics
        inputs.put("B11", 2.0);
        inputs.put("B12", 2.0);
        inputs.put("B13", 2.0);
        inputs.put("B21", 2.0);
        inputs.put("B22", 2.0);
        inputs.put("B23", 2.0);
        inputs.put("B24", 2.0);
        inputs.put("B25", 2.0);
        inputs.put("B26", 2.0);
        inputs.put("B31", 2.0);
        inputs.put("B32", 2.0);
        inputs.put("B33", 2.0);
        inputs.put("B34", 2.0);
        inputs.put("B41", 2.0);
        inputs.put("B42", 2.0);
        inputs.put("B43", 2.0);
        inputs.put("B44", 2.0);

        // M submetrics
        inputs.put("M11", 1.0);
        inputs.put("M12", 2.0);
        inputs.put("M13", 3.0);
        inputs.put("M14", 4.0);
        inputs.put("M21", 5.0);
        inputs.put("M22", 6.0);
        inputs.put("M23", 7.0);
        inputs.put("M31", 8.0);
        inputs.put("M32", 9.0);
        inputs.put("M33", 10.0);

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));

        assertEquals(1.2, out.get("KI"), 1e-9);
        assertEquals(1.1, out.get("KI_A"), 1e-9);
        assertEquals(1.3, out.get("KI_B"), 1e-9);
        assertEquals(1.4, out.get("KI_M"), 1e-9);

        assertNotEquals(100.0, out.get("A_TOTAL"), 1e-9);
        assertNotEquals(200.0, out.get("B_TOTAL"), 1e-9);
        assertNotEquals(300.0, out.get("M_TOTAL"), 1e-9);

        assertEquals(out.get("A_TOTAL") * 1.1, out.get("A_TOTAL_WITH_KI"), 1e-9);
        assertEquals(out.get("B_TOTAL") * 1.3, out.get("B_TOTAL_WITH_KI"), 1e-9);
        assertEquals(out.get("M_TOTAL") * 1.4, out.get("M_TOTAL_WITH_KI"), 1e-9);
    }
}
