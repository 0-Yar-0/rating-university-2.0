package ru.ystu.rating.university.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.DocumentCalcDto;
import ru.ystu.rating.university.dto.DocumentParamsDto;
import ru.ystu.rating.university.dto.HistoryIterationDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.model.*;
import ru.ystu.rating.university.repository.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for class M parameters and calculations.
 */
@Service
public class MService {

    private final AppUserRepository userRepo;
    private final RatingClassRepository classRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final ParamsSetRepository paramsRepo;
    private final CalcResultRepository calcRepo;
    private final DocumentService documentService;

    public MService(AppUserRepository userRepo,
                    RatingClassRepository classRepo,
                    DataRepository dataRepo,
                    UserIterStateRepository userIterStateRepo,
                    ParamsSetRepository paramsRepo,
                    CalcResultRepository calcRepo,
                    DocumentService documentService) {
        this.userRepo = userRepo;
        this.classRepo = classRepo;
        this.dataRepo = dataRepo;
        this.userIterStateRepo = userIterStateRepo;
        this.paramsRepo = paramsRepo;
        this.calcRepo = calcRepo;
        this.documentService = documentService;
    }

    @Transactional
    public List<Map<String, Object>> saveParamsAndComputeForM(
            AppUser user,
            int iter,
            List<Map<String, Object>> yearParams
    ) {
        if (yearParams == null || yearParams.isEmpty()) {
            return List.of();
        }

        RatingClass mClass = classRepo.findByCode("M")
                .orElseThrow(() -> new IllegalStateException("Класс M не найден"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> p : yearParams) {
            Integer year = toIntOrNull(p.get("year"));
            if (year == null) {
                continue;
            }

            Data d = new Data();
            d.setAppUser(user);
            d.setClassType(mClass);
            d.setIter(iter);
            d.setYearData(year);
            d = dataRepo.save(d);

            ParamsSet ps = new ParamsSet();
            ps.setData(d);
            Map<String, Object> raw = new HashMap<>(p);
            raw.remove("year");
            raw.remove("iteration");
            ps.setParams(raw);
            paramsRepo.save(ps);

            Map<String, Double> numeric = toNumericMap(raw);
            DocumentCalcDto dcalc = documentService.computeAll(new DocumentParamsDto(numeric));

            double m11Final = metricOrDefault(raw, "M11", dcalc.get("M11"));
            double m12Final = metricOrDefault(raw, "M12", dcalc.get("M12"));
            double m13Final = metricOrDefault(raw, "M13", dcalc.get("M13"));
            double m14Final = metricOrDefault(raw, "M14", dcalc.get("M14"));
            double m21Final = metricOrDefault(raw, "M21", dcalc.get("M21"));
            double m22Final = metricOrDefault(raw, "M22", dcalc.get("M22"));
            double m23Final = metricOrDefault(raw, "M23", dcalc.get("M23"));
            double m24Final = metricOrDefault(raw, "M24", dcalc.get("M24"));
            double m25Final = metricOrDefault(raw, "M25", dcalc.get("M25"));
            double m26Final = metricOrDefault(raw, "M26", dcalc.get("M26"));
            double m27Final = metricOrDefault(raw, "M27", dcalc.get("M27"));
            double m31Final = metricOrDefault(raw, "M31", dcalc.get("M31"));
            double m32Final = metricOrDefault(raw, "M32", dcalc.get("M32"));
            double m33Final = metricOrDefault(raw, "M33", dcalc.get("M33"));
            double m41Final = metricOrDefault(raw, "M41", dcalc.get("M41"));
            double m42Final = metricOrDefault(raw, "M42", dcalc.get("M42"));
            double m43Final = metricOrDefault(raw, "M43", dcalc.get("M43"));
            double m44Final = metricOrDefault(raw, "M44", dcalc.get("M44"));

            Double kiOverride = firstNonNull(
                    toDoubleOrNull(raw.get("M_KI")),
                    toDoubleOrNull(raw.get("KI_M")),
                    toDoubleOrNull(raw.get("KI"))
            );
            double mKi = kiOverride != null ? kiOverride : dcalc.get("KI_M");
            double mTotal = m11Final + m12Final + m13Final + m14Final
                    + m21Final + m22Final + m23Final + m24Final
                    + m25Final + m26Final + m27Final
                    + m31Final + m32Final + m33Final
                    + m41Final + m42Final + m43Final + m44Final;
            double mTotalWithKi = mTotal * mKi;

            Map<String, Object> calc = new LinkedHashMap<>();
            calc.put("year", year);
            calc.put("iteration", iter);
            calc.put("M11", m11Final);
            calc.put("M12", m12Final);
            calc.put("M13", m13Final);
            calc.put("M14", m14Final);
            calc.put("M21", m21Final);
            calc.put("M22", m22Final);
            calc.put("M23", m23Final);
            calc.put("M24", m24Final);
            calc.put("M25", m25Final);
            calc.put("M26", m26Final);
            calc.put("M27", m27Final);
            calc.put("M31", m31Final);
            calc.put("M32", m32Final);
            calc.put("M33", m33Final);
            calc.put("M41", m41Final);
            calc.put("M42", m42Final);
            calc.put("M43", m43Final);
            calc.put("M44", m44Final);
            calc.put("KI", mKi);
            calc.put("TOTAL", mTotalWithKi);
            calc.put("M_TOTAL", mTotal);
            calc.put("M_TOTAL_WITH_KI", mTotalWithKi);

            CalcResult cr = new CalcResult();
            cr.setData(d);
            cr.setCalcParams(calc);
            calcRepo.save(cr);

            result.add(calc);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ClassParamsBlockDto getLastParamsForM(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();
        RatingClass mClass = classRepo.findByCode("M")
                .orElseThrow(() -> new IllegalStateException("Класс M не найден"));

        Integer currentIter = userIterStateRepo.findByAppUser(user)
                .map(UserIterState::getCurrentIter)
                .orElse(null);
        if (currentIter == null || currentIter == 0) {
            return new ClassParamsBlockDto("M", List.of(), null);
        }

        List<Data> rows = dataRepo.findAllByAppUserAndClassTypeAndIterOrderByYearDataAsc(
                user, mClass, currentIter
        );

        List<Map<String, Object>> dtoList = rows.stream()
                .map(d -> {
                    Map<String, Object> json = new HashMap<>(d.getParamsSet().getParams());
                    json.put("year", d.getYearData());
                    return json;
                })
                .toList();

        return new ClassParamsBlockDto("M", dtoList, null);
    }

    @Transactional(readOnly = true)
    public HistoryResponseDto getHistoryForM(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();
        RatingClass mClass = classRepo.findByCode("M")
                .orElseThrow(() -> new IllegalStateException("Класс M не найден"));

        List<Data> all = dataRepo.findAllByAppUserOrderByIterAscClassTypeIdAscYearDataAsc(user);
        List<Data> onlyM = all.stream()
                .filter(d -> d.getClassType().getId().equals(mClass.getId()))
                .toList();

        Map<Integer, List<Data>> byIter = onlyM.stream()
                .collect(Collectors.groupingBy(
                        Data::getIter,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<HistoryIterationDto> iterations = new ArrayList<>();
        for (Map.Entry<Integer, List<Data>> e : byIter.entrySet()) {
            Integer iter = e.getKey();
            List<Map<String, Object>> rows = e.getValue().stream()
                    .map(d -> {
                        CalcResult cr = d.getCalcResult();
                        return cr == null ? null : cr.getCalcParams();
                    })
                    .filter(Objects::nonNull)
                    .toList();
            iterations.add(new HistoryIterationDto(iter, rows));
        }

        return new HistoryResponseDto("M", iterations);
    }

    private static Integer toIntOrNull(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        String s = value.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double toDoubleOrNull(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        String s = value.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return Double.parseDouble(s.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<String, Double> toNumericMap(Map<String, Object> src) {
        Map<String, Double> out = new HashMap<>();
        for (Map.Entry<String, Object> e : src.entrySet()) {
            Double v = toDoubleOrNull(e.getValue());
            if (v != null) {
                out.put(e.getKey(), v);
            }
        }
        return out;
    }

    private static double metricOrDefault(Map<String, Object> rawParams, String key, double fallback) {
        if (rawParams == null || rawParams.isEmpty()) {
            return fallback;
        }
        Double direct = toDoubleOrNull(rawParams.get(key));
        if (direct == null) {
            direct = toDoubleOrNull(rawParams.get(key.toLowerCase()));
        }
        return direct == null ? fallback : direct;
    }

    private static Double firstNonNull(Double... values) {
        for (Double value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
