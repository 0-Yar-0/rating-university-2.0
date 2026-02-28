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

        DocumentCalcDto out = svc.computeAll(new DocumentParamsDto(inputs));
        assertEquals(941.5, out.get("PN"), 1e-6);
        assertEquals(19.3, out.get("DI"), 1e-6);
        assertEquals(1.0, out.get("KI"), 1e-6);
        assertEquals(57.771, out.get("TOTAL"), 1e-6);
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
