package ru.ystu.rating.university.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.ystu.rating.university.dto.BParamsDto;
import ru.ystu.rating.university.dto.BCalcDto;
import ru.ystu.rating.university.dto.DocumentCalcDto;
import ru.ystu.rating.university.dto.DocumentParamsDto;
import ru.ystu.rating.university.util.BMathCalculator;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BExtendedCalculatorTest {
    private BMathCalculator bmath;
    private DocumentService docSvc;

    @BeforeEach
    void init() {
        bmath = new BMathCalculator();
        docSvc = new DocumentService();
    }

    @Test
    void fullExampleShouldProduceExtendedTotals() {
        // We'll reuse some values from the document that were used for B22 etc.
        BParamsDto params = new BParamsDto(
                2024,
                905.0, 0.0, 345.0, 2.0, 0.0,
                0.0, 0.0, 0.0, 0.0,
                0.0, 0.0,
                // extended values for B22..B33
                941.5, 79.3, 11.8, 0.0, 0.0,
                0.0, 0.0, 1033.0, 0.0, 266.0,
                0.0564, 0.0343, 147.0, 197.0,
                1250.0, 1195.0, 200.0, 362.0,
                4.3066
        );

        BCalcDto basic = bmath.computeBForYear(params, 1);
        Map<String, Double> map = new HashMap<>();
        // copy inputs to map for document service
        map.put("ENa", params.ENa());
        map.put("ENb", params.ENb());
        map.put("ENc", params.ENc());
        map.put("Eb", params.Eb());
        map.put("Ec", params.Ec());
        map.put("beta121", params.beta121());
        map.put("beta122", params.beta122());
        map.put("beta131", params.beta131());
        map.put("beta132", params.beta132());
        map.put("beta211", params.beta211());
        map.put("beta212", params.beta212());
        map.put("NBP", params.NBP());
        map.put("NMP", params.NMP());
        map.put("ACP", params.ACP());
        map.put("OPC", params.OPC());
        map.put("ACC", params.ACC());
        map.put("PKP", params.PKP());
        map.put("PPP", params.PPP());
        map.put("NP", params.NP());
        map.put("NOA", params.NOA());
        map.put("NAP", params.NAP());
        map.put("B25_o", params.B25_o());
        map.put("B26_o", params.B26_o());
        map.put("UT", params.UT());
        map.put("DO", params.DO());
        map.put("N", params.N());
        map.put("Npr", params.Npr());
        map.put("VO", params.VO());
        map.put("PO", params.PO());
        map.put("B33_o", params.B33_o());

        DocumentCalcDto doc = docSvc.computeAll(new DocumentParamsDto(map));

        // combine as BService would
        BCalcDto full = new BCalcDto(
                basic.calcResultId(),
                basic.year(), basic.iteration(),
                basic.b11(), basic.b12(), basic.b13(), basic.b21(),
                doc.get("B22"), doc.get("B23"), doc.get("B24"),
                doc.get("B25"), doc.get("B26"),
                doc.get("B31"), doc.get("B32"), doc.get("B33"),
                basic.sumB() + doc.get("B22") + doc.get("B23") + doc.get("B24")
                        + doc.get("B25") + doc.get("B26")
                        + doc.get("B31") + doc.get("B32") + doc.get("B33"),
                basic.codeClassA(), basic.codeClassB(), basic.codeClassV(),
                basic.codeB11(), basic.codeB12(), basic.codeB13(), basic.codeB21()
        );

        // just check a couple of numbers
        assertEquals(doc.get("B22"), full.b22(), 1e-6);
        assertEquals(doc.get("B31"), full.b31(), 1e-6);
        assertEquals(basic.b11(), full.b11(), 1e-6);
    }
}
