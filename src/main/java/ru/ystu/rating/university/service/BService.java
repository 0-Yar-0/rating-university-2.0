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
        if (yearParams == null || yearParams.isEmpty()) {
            return List.of();
        }

        RatingClass bClass = classRepo.findByCode("B")
                .orElseThrow(() -> new IllegalStateException("Класс B не найден"));

        List<BCalcDto> result = new ArrayList<>();

        for (BParamsDto p : yearParams) {

            Data d = new Data();
            d.setAppUser(user);
            d.setClassType(bClass);
            d.setIter(iter);
            d.setYearData(p.year());
            d = dataRepo.save(d);

            ParamsSet ps = new ParamsSet();
            ps.setData(d);
            ps.setParams(BParamsMapper.toJson(p));
            paramsRepo.save(ps);

            BCalcDto calcDto = bMathCalculator.computeBForYear(p, iter);

            // build a generic map of parameters so we can ask DocumentService to
            // produce the extended metrics; we copy everything that may be
            // relevant from BParamsDto.
            Map<String, Double> paramMap = new HashMap<>();
            paramMap.put("ENa", p.ENa());
            paramMap.put("ENb", p.ENb());
            paramMap.put("ENc", p.ENc());
            paramMap.put("Eb", p.Eb());
            paramMap.put("Ec", p.Ec());
            paramMap.put("beta121", p.beta121());
            paramMap.put("beta122", p.beta122());
            paramMap.put("beta131", p.beta131());
            paramMap.put("beta132", p.beta132());
            paramMap.put("beta211", p.beta211());
            paramMap.put("beta212", p.beta212());
            // extra fields
            paramMap.put("NBP", p.NBP());
            paramMap.put("NMP", p.NMP());
            paramMap.put("ACP", p.ACP());
            paramMap.put("OPC", p.OPC());
            paramMap.put("ACC", p.ACC());
            paramMap.put("PKP", p.PKP());
            paramMap.put("PPP", p.PPP());
            paramMap.put("NP", p.NP());
            paramMap.put("NOA", p.NOA());
            paramMap.put("NAP", p.NAP());
            paramMap.put("B25_o", p.B25_o());
            paramMap.put("B26_o", p.B26_o());
            paramMap.put("UT", p.UT());
            paramMap.put("DO", p.DO());
            paramMap.put("N", p.N());
            paramMap.put("Npr", p.Npr());
            paramMap.put("VO", p.VO());
            paramMap.put("PO", p.PO());
            paramMap.put("B33_o", p.B33_o());

            DocumentCalcDto docCalc = documentService.computeAll(new DocumentParamsDto(paramMap));

            // transfer additional metrics into calcDto by rebuilding it - easier
            calcDto = new BCalcDto(
                    calcDto.calcResultId(),
                    calcDto.year(),
                    calcDto.iteration(),
                    calcDto.b11(),
                    calcDto.b12(),
                    calcDto.b13(),
                    calcDto.b21(),
                    docCalc.get("B22"),
                    docCalc.get("B23"),
                    docCalc.get("B24"),
                    docCalc.get("B25"),
                    docCalc.get("B26"),
                    docCalc.get("B31"),
                    docCalc.get("B32"),
                    docCalc.get("B33"),
                    calcDto.sumB() +
                            docCalc.get("B22") + docCalc.get("B23") + docCalc.get("B24")
                                    + docCalc.get("B25") + docCalc.get("B26")
                                    + docCalc.get("B31") + docCalc.get("B32") + docCalc.get("B33"),
                    calcDto.codeClassA(),
                    calcDto.codeClassB(),
                    calcDto.codeClassV(),
                    calcDto.codeB11(),
                    calcDto.codeB12(),
                    calcDto.codeB13(),
                    calcDto.codeB21()
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

        List<BParamsDto> dtoList = rows.stream()
                .map(d -> {
                    Map<String, Object> json = d.getParamsSet().getParams();
                    return BParamsMapper.fromJson(d.getYearData(), json);
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

        List<BParamsDto> dtoList = rows.stream()
                .map(d -> {
                    Map<String, Object> json = d.getParamsSet().getParams();
                    return BParamsMapper.fromJson(d.getYearData(), json);
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

        namesRepo.save(names);
    }
}
