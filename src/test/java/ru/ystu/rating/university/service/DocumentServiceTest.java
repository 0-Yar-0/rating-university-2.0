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
        // verify additional formulas produce expected values
        assertEquals(0.95, out.get("B34"), 1e-6);
        assertEquals(24.15, out.get("B41"), 1e-2);
        assertEquals(741.6, out.get("B42"), 1e-1);
        assertEquals(1.43, out.get("B43"), 1e-2);
        assertEquals(281.8, out.get("B44"), 1e-1);
    }

    @Test
    void testB22Computation() {
        Map<String, Double> inputs = new HashMap<>();
        inputs.put("NBP", 941.5);
        inputs.put("NMP", 79.3);
        inputs.put("ACP", 11.8);
        inputs.put("OPC", 0.0);
        inputs.put("ACC", 0.0);
        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));
        // raw value from document 0.1219, weighted 0.488
        assertEquals(0.1219, out.get("B22_raw"), 1e-4);
        assertEquals(0.488, out.get("B22"), 1e-3);
        // other metrics already validated in first test
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
    }
}
