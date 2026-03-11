package ru.ystu.rating.university.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ystu.rating.university.dto.*;
import ru.ystu.rating.university.mapper.BCalcMapper;
import ru.ystu.rating.university.mapper.BParamsMapper;
import ru.ystu.rating.university.model.*;
import ru.ystu.rating.university.repository.*;
import ru.ystu.rating.university.util.BMathCalculator;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BService {

    private final AppUserRepository userRepo;
    private final RatingClassRepository classRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final ParamsSetRepository paramsRepo;
    private final CalcResultRepository calcRepo;
    private final CalcResultNameRepository namesRepo;
    private final BMathCalculator bMathCalculator;
    private final DocumentService documentService;

    // helper used in several methods for building maps
    private static void putIfNotNull(Map<String, Double> m, String key, Double val) {
        if (val != null) {
            m.put(key, val);
        }
    }

    public BService(AppUserRepository userRepo,
                    RatingClassRepository classRepo,
                    DataRepository dataRepo,
                    UserIterStateRepository userIterStateRepo,
                    ParamsSetRepository paramsRepo,
                    CalcResultRepository calcRepo,
                    CalcResultNameRepository namesRepo,
                    BMathCalculator bMathCalculator,
                    DocumentService documentService) {
        this.userRepo = userRepo;
        this.classRepo = classRepo;
        this.dataRepo = dataRepo;
        this.userIterStateRepo = userIterStateRepo;
        this.paramsRepo = paramsRepo;
        this.calcRepo = calcRepo;
        this.namesRepo = namesRepo;
        this.bMathCalculator = bMathCalculator;
        this.documentService = documentService;
    }

    // ========================================================================
    // 1. СОХРАНЕНИЕ ПАРАМЕТРОВ КЛАССА B + РАСЧЁТ
    // ========================================================================

    /**
     * Сохраняет параметры ТОЛЬКО для класса B в заданную итерацию и
     * выполняет расчёт по этим параметрам.
     *
     * @param user       пользователь
     * @param iter       номер итерации (уже посчитан снаружи)
     * @param yearParams список параметров по годам для класса B
     * @return список рассчитанных BCalcDto по годам
     */
    @Transactional
    public List<BCalcDto> saveParamsAndComputeForB(AppUser user, int iter, List<BParamsDto> yearParams, BMetricNamesDto namesDto) {
                return saveParamsAndComputeForB(user, iter, yearParams, null, namesDto);
        }

        @Transactional
        public List<BCalcDto> saveParamsAndComputeForB(AppUser user, int iter, List<BParamsDto> yearParams, List<Map<String, Object>> rawYearParams, BMetricNamesDto namesDto) {
        if (yearParams == null || yearParams.isEmpty()) {
            return List.of();
        }

        RatingClass bClass = classRepo.findByCode("B")
                .orElseThrow(() -> new IllegalStateException("Класс B не найден"));

        List<BCalcDto> result = new ArrayList<>();

                for (int idx = 0; idx < yearParams.size(); idx++) {
                        BParamsDto p = yearParams.get(idx);

            // if the client resubmits parameters for the same year/iter we should
            // overwrite existing row rather than blow up with constraint violation
            dataRepo.deleteByAppUserAndClassTypeAndIterAndYearData(
                    user, bClass, iter, p.year());

            Data d = new Data();
            d.setAppUser(user);
            d.setClassType(bClass);
            d.setIter(iter);
            d.setYearData(p.year());
            d = dataRepo.save(d);

            ParamsSet ps = new ParamsSet();
            ps.setData(d);
                        Map<String, Object> rawParams = BParamsMapper.toJson(p);
                        if (rawYearParams != null && idx < rawYearParams.size() && rawYearParams.get(idx) != null) {
                                rawParams = new HashMap<>(rawYearParams.get(idx));
                        }
                        rawParams.remove("year");
                        rawParams.remove("iteration");
                        ps.setParams(rawParams);
            paramsRepo.save(ps);

            BCalcDto calcDto = bMathCalculator.computeBForYear(p, iter);

            // build a generic map of parameters so we can ask DocumentService to
            // produce the extended metrics; we copy everything that may be
            // relevant from BParamsDto.
            Map<String, Double> paramMap = new HashMap<>();
                        for (Map.Entry<String, Object> e : rawParams.entrySet()) {
                                Double num = toDoubleOrNull(e.getValue());
                                if (num != null) {
                                        paramMap.put(e.getKey(), num);
                                }
                        }
            putIfNotNull(paramMap, "ENa", p.ENa());
            putIfNotNull(paramMap, "ENb", p.ENb());
            putIfNotNull(paramMap, "ENc", p.ENc());
            putIfNotNull(paramMap, "Eb", p.Eb());
            putIfNotNull(paramMap, "Ec", p.Ec());
            putIfNotNull(paramMap, "beta121", p.beta121());
            putIfNotNull(paramMap, "beta122", p.beta122());
            putIfNotNull(paramMap, "beta131", p.beta131());
            putIfNotNull(paramMap, "beta132", p.beta132());
            putIfNotNull(paramMap, "beta211", p.beta211());
            putIfNotNull(paramMap, "beta212", p.beta212());
            // extra fields
            putIfNotNull(paramMap, "NBP", p.NBP());
            putIfNotNull(paramMap, "NMP", p.NMP());
            putIfNotNull(paramMap, "ACP", p.ACP());
            putIfNotNull(paramMap, "OPC", p.OPC());
            putIfNotNull(paramMap, "ACC", p.ACC());
            putIfNotNull(paramMap, "PKP", p.PKP());
            putIfNotNull(paramMap, "PPP", p.PPP());
            putIfNotNull(paramMap, "NP", p.NP());
            putIfNotNull(paramMap, "NOA", p.NOA());
            putIfNotNull(paramMap, "NAP", p.NAP());
            putIfNotNull(paramMap, "B25_o", p.B25_o());
            putIfNotNull(paramMap, "B26_o", p.B26_o());
            putIfNotNull(paramMap, "UT", p.UT());
            putIfNotNull(paramMap, "DO", p.DO());
            putIfNotNull(paramMap, "N", p.N());
            putIfNotNull(paramMap, "Npr", p.Npr());
            putIfNotNull(paramMap, "VO", p.VO());
            putIfNotNull(paramMap, "PO", p.PO());
            putIfNotNull(paramMap, "B33_o", p.B33_o());
            putIfNotNull(paramMap, "NBo", p.NBo());
            putIfNotNull(paramMap, "NBv", p.NBv());
            putIfNotNull(paramMap, "NBz", p.NBz());
            putIfNotNull(paramMap, "NMo", p.NMo());
            putIfNotNull(paramMap, "NMv", p.NMv());
            putIfNotNull(paramMap, "NMz", p.NMz());
            putIfNotNull(paramMap, "ACo", p.ACo());
            putIfNotNull(paramMap, "ACv", p.ACv());
            putIfNotNull(paramMap, "ACz", p.ACz());
            putIfNotNull(paramMap, "KPo", p.KPo());
            putIfNotNull(paramMap, "KPv", p.KPv());
            putIfNotNull(paramMap, "KPz", p.KPz());
            putIfNotNull(paramMap, "PPPo", p.PPPo());
            putIfNotNull(paramMap, "PPPv", p.PPPv());
            putIfNotNull(paramMap, "PPPz", p.PPPz());
            putIfNotNull(paramMap, "NPo", p.NPo());
            putIfNotNull(paramMap, "NPv", p.NPv());
            putIfNotNull(paramMap, "NPz", p.NPz());
            putIfNotNull(paramMap, "NAo", p.NAo());
            putIfNotNull(paramMap, "NAv", p.NAv());
            putIfNotNull(paramMap, "NAz", p.NAz());
            putIfNotNull(paramMap, "PNo", p.PNo());
            putIfNotNull(paramMap, "PNv", p.PNv());
            putIfNotNull(paramMap, "PNz", p.PNz());
            putIfNotNull(paramMap, "Po", p.Po());
            putIfNotNull(paramMap, "Pv", p.Pv());
            putIfNotNull(paramMap, "Pz", p.Pz());
            putIfNotNull(paramMap, "DIo", p.DIo());
            putIfNotNull(paramMap, "DIv", p.DIv());
            putIfNotNull(paramMap, "DIz", p.DIz());
            // additional indicators for B34..B44
            putIfNotNull(paramMap, "NR2023", p.NR2023());
            putIfNotNull(paramMap, "NR2024", p.NR2024());
            putIfNotNull(paramMap, "NR2025", p.NR2025());
            putIfNotNull(paramMap, "WL2022", p.WL2022());
            putIfNotNull(paramMap, "WL2023", p.WL2023());
            putIfNotNull(paramMap, "WL2024", p.WL2024());
            putIfNotNull(paramMap, "NPR2022", p.NPR2022());
            putIfNotNull(paramMap, "NPR2023", p.NPR2023());
            putIfNotNull(paramMap, "NPR2024", p.NPR2024());
            putIfNotNull(paramMap, "DN2022", p.DN2022());
            putIfNotNull(paramMap, "DN2023", p.DN2023());
            putIfNotNull(paramMap, "DN2024", p.DN2024());
            putIfNotNull(paramMap, "Io", p.Io());
            putIfNotNull(paramMap, "Iv", p.Iv());
            putIfNotNull(paramMap, "Iz", p.Iz());
            putIfNotNull(paramMap, "No", p.No());
            putIfNotNull(paramMap, "Nv", p.Nv());
            putIfNotNull(paramMap, "Nz", p.Nz());
            putIfNotNull(paramMap, "OD2022", p.OD2022());
            putIfNotNull(paramMap, "OD2023", p.OD2023());
            putIfNotNull(paramMap, "OD2024", p.OD2024());
            putIfNotNull(paramMap, "PN2022", p.PN2022());
            putIfNotNull(paramMap, "PN2023", p.PN2023());
            putIfNotNull(paramMap, "PN2024", p.PN2024());

            DocumentCalcDto docCalc = documentService.computeAll(new DocumentParamsDto(paramMap));

            double b11Final = metricOrDefault(rawParams, "B11", calcDto.b11());
            double b12Final = metricOrDefault(rawParams, "B12", calcDto.b12());
            double b13Final = metricOrDefault(rawParams, "B13", calcDto.b13());
            double b21Final = metricOrDefault(rawParams, "B21", calcDto.b21());
            double b22Final = metricOrDefault(rawParams, "B22", docCalc.get("B22"));
            double b23Final = metricOrDefault(rawParams, "B23", docCalc.get("B23"));
            double b24Final = metricOrDefault(rawParams, "B24", docCalc.get("B24"));
            double b25Final = metricOrDefault(rawParams, "B25", docCalc.get("B25"));
            double b26Final = metricOrDefault(rawParams, "B26", docCalc.get("B26"));
            double b31Final = metricOrDefault(rawParams, "B31", docCalc.get("B31"));
            double b32Final = metricOrDefault(rawParams, "B32", docCalc.get("B32"));
            double b33Final = metricOrDefault(rawParams, "B33", docCalc.get("B33"));
            double b34Final = metricOrDefault(rawParams, "B34", docCalc.get("B34"));
            double b41Final = metricOrDefault(rawParams, "B41", docCalc.get("B41"));
            double b42Final = metricOrDefault(rawParams, "B42", docCalc.get("B42"));
            double b43Final = metricOrDefault(rawParams, "B43", docCalc.get("B43"));
            double b44Final = metricOrDefault(rawParams, "B44", docCalc.get("B44"));
            double sumBFinal = b11Final + b12Final + b13Final + b21Final
                    + b22Final + b23Final + b24Final
                    + b25Final + b26Final
                    + b31Final + b32Final + b33Final
                    + b34Final + b41Final + b42Final
                    + b43Final + b44Final;

            // transfer additional metrics into calcDto by rebuilding it - easier
            calcDto = new BCalcDto(
                    calcDto.calcResultId(),
                    calcDto.year(),
                    calcDto.iteration(),
                    b11Final,
                    b12Final,
                    b13Final,
                    b21Final,
                    b22Final,
                    b23Final,
                    b24Final,
                    b25Final,
                    b26Final,
                    b31Final,
                    b32Final,
                    b33Final,
                    b34Final,
                    b41Final,
                    b42Final,
                    b43Final,
                    b44Final,
                    sumBFinal,
                    calcDto.codeClassA(),
                    calcDto.codeClassB(),
                    calcDto.codeClassV(),
                    calcDto.codeB11(),
                    calcDto.codeB12(),
                    calcDto.codeB13(),
                    calcDto.codeB21(),
                    calcDto.codeB22(),
                    calcDto.codeB23(),
                    calcDto.codeB24(),
                    calcDto.codeB25(),
                    calcDto.codeB26(),
                    calcDto.codeB31(),
                    calcDto.codeB32(),
                    calcDto.codeB33(),
                    calcDto.codeB34(),
                    calcDto.codeB41(),
                    calcDto.codeB42(),
                    calcDto.codeB43(),
                    calcDto.codeB44()
            );

            CalcResult cr = new CalcResult();
            cr.setData(d);
            cr.setCalcParams(BCalcMapper.toCalcJson(calcDto));
            calcRepo.save(cr);

            if(namesDto != null){
                CalcResultName names = new CalcResultName();
                names.setCalcResult(cr);
                names.setCodeClassA(namesDto.codeClassA());
                names.setCodeClassB(namesDto.codeClassB());
                names.setCodeClassV(namesDto.codeClassV());
                names.setCodeB11(namesDto.codeB11());
                names.setCodeB12(namesDto.codeB12());
                names.setCodeB13(namesDto.codeB13());
                names.setCodeB21(namesDto.codeB21());
                names.setCodeB22(namesDto.codeB22());
                names.setCodeB23(namesDto.codeB23());
                names.setCodeB24(namesDto.codeB24());
                names.setCodeB25(namesDto.codeB25());
                names.setCodeB26(namesDto.codeB26());
                names.setCodeB31(namesDto.codeB31());
                names.setCodeB32(namesDto.codeB32());
                names.setCodeB33(namesDto.codeB33());
                names.setCodeB34(namesDto.codeB34());
                names.setCodeB41(namesDto.codeB41());
                names.setCodeB42(namesDto.codeB42());
                names.setCodeB43(namesDto.codeB43());
                names.setCodeB44(namesDto.codeB44());
                namesRepo.save(names);
            }

            result.add(calcDto);
        }

        return result;
    }

    // ========================================================================
    // 2. ПОЛУЧИТЬ ПОСЛЕДНИЕ ВВЕДЁННЫЕ ПАРАМЕТРЫ КЛАССА B
    // ========================================================================

    @Transactional(readOnly = true)
    public ClassParamsBlockDto getLastParamsForB(Long userId) {
        AppUser user = userRepo.findById(userId).orElseThrow();
        RatingClass bClass = classRepo.findByCode("B")
                .orElseThrow(() -> new IllegalStateException("Класс B не найден"));

        Integer currentIterB = userIterStateRepo.findByAppUser(user)
                .map(UserIterState::getCurrentIter)
                .orElse(null);
        if (currentIterB == null || currentIterB == 0) {
            return new ClassParamsBlockDto("B", List.of(), null);
        }

        List<Data> rows = dataRepo.findAllByAppUserAndClassTypeAndIterOrderByYearDataAsc(
                user, bClass, currentIterB
        );

                List<Map<String, Object>> dtoList = rows.stream()
                .map(d -> {
                                        Map<String, Object> json = new HashMap<>(d.getParamsSet().getParams());
                                        json.put("year", d.getYearData());
                                        return json;
                })
                .toList();

        return new ClassParamsBlockDto("B", dtoList, null);
    }

    @Transactional(readOnly = true)
    public ClassParamsBlockDto getParamsForBIter(Long userId, int iter) {
        AppUser user = userRepo.findById(userId).orElseThrow();
        RatingClass bClass = classRepo.findByCode("B")
                .orElseThrow(() -> new IllegalStateException("Класс B не найден"));

        List<Data> rows = dataRepo.findAllByAppUserAndClassTypeAndIterOrderByYearDataAsc(
                user, bClass, iter
        );

        if (rows.isEmpty()) {
            return new ClassParamsBlockDto("B", List.of(), null);
        }

                List<Map<String, Object>> dtoList = rows.stream()
                .map(d -> {
                                        Map<String, Object> json = new HashMap<>(d.getParamsSet().getParams());
                                        json.put("year", d.getYearData());
                                        return json;
                })
                .toList();

        return new ClassParamsBlockDto("B", dtoList, null);
    }


    // ========================================================================
    // 3. ПОЛУЧИТЬ ПОСЛЕДНИЕ РАСЧЁТЫ КЛАССА B
    // ========================================================================

    @Transactional(readOnly = true)
    public ClassCalcBlockDto getLastCalcForB(Long userId) {
        AppUser user = userRepo.findById(userId).orElseThrow();
        RatingClass bClass = classRepo.findByCode("B")
                .orElseThrow(() -> new IllegalStateException("Класс B не найден"));

        Integer currentIterB = userIterStateRepo.findByAppUser(user)
                .map(UserIterState::getCurrentIter)
                .orElse(null);
        if (currentIterB == null || currentIterB == 0) {
            return new ClassCalcBlockDto("B", List.of());
        }

        List<Data> rows = dataRepo.findAllByAppUserAndClassTypeAndIterOrderByYearDataAsc(
                user, bClass, currentIterB
        );

        List<BCalcDto> dtoList = rows.stream()
                .map(d -> {
                    CalcResult cr = d.getCalcResult();
                    if (cr == null) return null;
                    CalcResultName names = cr.getLabels();
                    Map<String, Object> json = cr.getCalcParams();
                    return BCalcMapper.fromCalcJson(cr.getId(), d.getYearData(), d.getIter(), json, names);
                })
                .filter(Objects::nonNull)
                .toList();

        return new ClassCalcBlockDto("B", dtoList);
    }

    // ========================================================================
    // 4. ИСТОРИЯ ПО КЛАССУ B (ВСЕ ИТЕРАЦИИ)
    // ========================================================================

    @Transactional(readOnly = true)
    public HistoryResponseDto getHistoryForB(Long userId) {
        AppUser user = userRepo.findById(userId).orElseThrow();
        RatingClass bClass = classRepo.findByCode("B")
                .orElseThrow(() -> new IllegalStateException("Класс B не найден"));

        List<Data> all = dataRepo.findAllByAppUserOrderByIterAscClassTypeIdAscYearDataAsc(user);

        List<Data> onlyB = all.stream()
                .filter(d -> d.getClassType().getId().equals(bClass.getId()))
                .toList();

        Map<Integer, List<Data>> byIter = onlyB.stream()
                .collect(Collectors.groupingBy(
                        Data::getIter,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<HistoryIterationDto> iterations = new ArrayList<>();

        for (Map.Entry<Integer, List<Data>> e : byIter.entrySet()) {
            Integer iter = e.getKey();
            List<Data> rows = e.getValue();

            List<BCalcDto> calcDtos = rows.stream()
                    .map(d -> {
                        CalcResult cr = d.getCalcResult();
                        if (cr == null) return null;
                        CalcResultName names = cr.getLabels();
                        Map<String, Object> json = cr.getCalcParams();
                        return BCalcMapper.fromCalcJson(cr.getId(), d.getYearData(), d.getIter(), json, names);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            iterations.add(new HistoryIterationDto(iter, calcDtos));
        }

        return new HistoryResponseDto("B", iterations);
    }

    // ========================================================================
    // 5. ОБНОВЛЕНИЕ НАЗВАНИЙ МЕТРИК
    // ========================================================================

    @Transactional
    public void updateMetricNames(MetricNamesDto dto) {
        CalcResult cr = calcRepo.findById(dto.calcResultId())
                .orElseThrow(() -> new IllegalArgumentException("CalcResult not found: " + dto.calcResultId()));

        CalcResultName names = namesRepo.findByCalcResultId(cr.getId())
                .orElseGet(() -> {
                    CalcResultName n = new CalcResultName();
                    n.setCalcResult(cr);
                    return n;
                });

        names.setCodeClassA(dto.codeClassA());
        names.setCodeClassB(dto.codeClassB());
        names.setCodeClassV(dto.codeClassV());
        names.setCodeB11(dto.codeB11());
        names.setCodeB12(dto.codeB12());
        names.setCodeB13(dto.codeB13());
        names.setCodeB21(dto.codeB21());
                names.setCodeB22(dto.codeB22());
                names.setCodeB23(dto.codeB23());
                names.setCodeB24(dto.codeB24());
                names.setCodeB25(dto.codeB25());
                names.setCodeB26(dto.codeB26());
                names.setCodeB31(dto.codeB31());
                names.setCodeB32(dto.codeB32());
                names.setCodeB33(dto.codeB33());
                names.setCodeB34(dto.codeB34());
                names.setCodeB41(dto.codeB41());
                names.setCodeB42(dto.codeB42());
                names.setCodeB43(dto.codeB43());
                names.setCodeB44(dto.codeB44());
        namesRepo.save(names);
    }

        private static Double toDoubleOrNull(Object value) {
                if (value == null) {
                        return null;
                }
                if (value instanceof Number n) {
                        return n.doubleValue();
                }
                String s = value.toString().trim();
                if (s.isEmpty()) {
                        return null;
                }
                try {
                        return Double.parseDouble(s.replace(',', '.'));
                } catch (NumberFormatException ex) {
                        return null;
                }
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

}
