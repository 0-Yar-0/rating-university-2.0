package ru.ystu.rating.university.service;
//
//import org.junit.jupiter.api.Test;
//import ru.ystu.rating.university.util.BMetric;
//import ru.ystu.rating.university.dto.BInput;
//
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class RatingServiceTest {
//
//    RatingService svc = new RatingService();
//
//    @Test
//    void testBHappyPath() {
//        // Пример: ENa=5, ENb=3, ENc=2, Eb=75, Ec=60
//        // B121=95, B122=100, B131=0.2, B132=1.0, B211=0.8, B212=1.0
//        BInput in = new BInput(
//                5, 3, 2, 75, 60,
//                95, 100,
//                0.2, 1.0,
//                0.8, 1.0
//        );
//        Map<Object, Double> out = svc.computeB(in);
//
//        // Проверим численные значения ± допуск
//        assertEquals(20.47, out.get(BMetric.B11), 0.01);
//        assertEquals(2.25,  out.get(BMetric.B12), 0.01);
//        assertEquals(1.60,  out.get(BMetric.B13), 0.01);
//        assertEquals(1.60,  out.get(BMetric.B21), 0.01);
//        assertEquals(25.92, out.get("TOTAL_B"), 0.02);
//    }
//
//    @Test
//    void testCutOffBelowVmin() {
//        // Сильно низкие значения -> нормализация в 0
//        BInput in = new BInput(
//                0, 1, 0, 10, 10,
//                60, 100,
//                0.0, 1.0,
//                0.0, 1.0
//        );
//        Map<Object, Double> out = svc.computeB(in);
//        assertTrue(out.values().stream().filter(v -> v instanceof Double)
//                .allMatch(v -> (Double)v == 0.0));
//    }
//
//    @Test
//    void testSaturateAboveVmax() {
//        // Значения выше порогов → нормализация 1 и балл = весу
//        BInput in = new BInput(
//                1, 0, 0, 200, 0,
//                150, 100,
//                1.0, 1.0,
//                5.0, 1.0
//        );
//        Map<Object, Double> out = svc.computeB(in);
//        assertEquals(23.0, out.get(BMetric.B11));
//        assertEquals(3.0,  out.get(BMetric.B12));
//        assertEquals(4.0,  out.get(BMetric.B13));
//        assertEquals(2.0,  out.get(BMetric.B21));
//        assertEquals(32.0, out.get("TOTAL_B"));
//    }
//}
