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
 * Service for class A parameters and calculations.
 */
@Service
public class AService {

    private final AppUserRepository userRepo;
    private final RatingClassRepository classRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final ParamsSetRepository paramsRepo;
    private final CalcResultRepository calcRepo;
    private final DocumentService documentService;

    public AService(AppUserRepository userRepo,
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
    public List<Map<String, Object>> saveParamsAndComputeForA(
            AppUser user,
            int iter,
            List<Map<String, Object>> yearParams
    ) {
        if (yearParams == null || yearParams.isEmpty()) {
            return List.of();
        }

        RatingClass aClass = classRepo.findByCode("A")
                .orElseThrow(() -> new IllegalStateException("Класс A не найден"));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> p : yearParams) {
            Integer year = toIntOrNull(p.get("year"));
            if (year == null) {
                continue;
            }

            Data d = new Data();
            d.setAppUser(user);
            d.setClassType(aClass);
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
            DocumentParamsDto dparams = new DocumentParamsDto(numeric);
            DocumentCalcDto dcalc = documentService.computeAll(dparams);

            Map<String, Object> calc = new LinkedHashMap<>();
            calc.put("year", year);
            calc.put("iteration", iter);
            calc.put("PN", dcalc.get("PN"));
            calc.put("DI", dcalc.get("DI"));
            calc.put("KI", dcalc.get("KI"));
            calc.put("sumPoints", numeric.getOrDefault("sumPoints", 0.0));
            calc.put("TOTAL", dcalc.get("TOTAL"));
            calc.put("A31", dcalc.get("A31"));
            calc.put("A32", dcalc.get("A32"));
            calc.put("A33", dcalc.get("A33"));

            CalcResult cr = new CalcResult();
            cr.setData(d);
            cr.setCalcParams(calc);
            calcRepo.save(cr);
            result.add(calc);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public ClassParamsBlockDto getLastParamsForA(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();
        RatingClass aClass = classRepo.findByCode("A")
                .orElseThrow(() -> new IllegalStateException("Класс A не найден"));

        Integer currentIter = userIterStateRepo.findByAppUser(user)
                .map(UserIterState::getCurrentIter)
                .orElse(null);
        if (currentIter == null || currentIter == 0) {
            return new ClassParamsBlockDto("A", List.of(), null);
        }

        List<Data> rows = dataRepo.findAllByAppUserAndClassTypeAndIterOrderByYearDataAsc(
                user, aClass, currentIter
        );

        List<Map<String, Object>> dtoList = rows.stream()
                .map(d -> {
                    Map<String, Object> json = new HashMap<>(d.getParamsSet().getParams());
                    json.put("year", d.getYearData());
                    return json;
                })
                .toList();

        return new ClassParamsBlockDto("A", dtoList, null);
    }

    @Transactional(readOnly = true)
    public HistoryResponseDto getHistoryForA(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();
        RatingClass aClass = classRepo.findByCode("A")
                .orElseThrow(() -> new IllegalStateException("Класс A не найден"));

        List<Data> all = dataRepo.findAllByAppUserOrderByIterAscClassTypeIdAscYearDataAsc(user);
        List<Data> onlyA = all.stream()
                .filter(d -> d.getClassType().getId().equals(aClass.getId()))
                .toList();

        Map<Integer, List<Data>> byIter = onlyA.stream()
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

        return new HistoryResponseDto("A", iterations);
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
}
