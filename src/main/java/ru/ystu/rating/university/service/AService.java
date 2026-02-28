package ru.ystu.rating.university.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ystu.rating.university.dto.ACalcDto;
import ru.ystu.rating.university.dto.AParamsDto;
import ru.ystu.rating.university.dto.DocumentParamsDto;
import ru.ystu.rating.university.dto.DocumentCalcDto;
import ru.ystu.rating.university.model.*;
import ru.ystu.rating.university.repository.*;
import ru.ystu.rating.university.mapper.ACalcMapper;

import java.util.*;

/**
 * Analogous to BService but for the "A" class formulas (PN/DI/KI and overall
 * summary).  The heavy lifting is delegated to DocumentService, which actually
 * contains the algebra.
 */
@Service
public class AService {

    private final AppUserRepository userRepo;
    private final RatingClassRepository classRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final ParamsSetRepository paramsRepo;
    private final CalcResultRepository calcRepo;
    private final CalcResultNameRepository namesRepo;
    private final DocumentService documentService;

    public AService(AppUserRepository userRepo,
                    RatingClassRepository classRepo,
                    DataRepository dataRepo,
                    UserIterStateRepository userIterStateRepo,
                    ParamsSetRepository paramsRepo,
                    CalcResultRepository calcRepo,
                    CalcResultNameRepository namesRepo,
                    DocumentService documentService) {
        this.userRepo = userRepo;
        this.classRepo = classRepo;
        this.dataRepo = dataRepo;
        this.userIterStateRepo = userIterStateRepo;
        this.paramsRepo = paramsRepo;
        this.calcRepo = calcRepo;
        this.namesRepo = namesRepo;
        this.documentService = documentService;
    }

    @Transactional
    public List<ACalcDto> saveParamsAndComputeForA(AppUser user, int iter,
                                                    List<AParamsDto> yearParams,
                                                    // we don't use metric names for A yet
                                                    Object unused) {
        if (yearParams == null || yearParams.isEmpty()) {
            return List.of();
        }

        RatingClass aClass = classRepo.findByCode("A")
                .orElseThrow(() -> new IllegalStateException("Класс A не найден"));

        List<ACalcDto> result = new ArrayList<>();
        for (AParamsDto p : yearParams) {
            Data d = new Data();
            d.setAppUser(user);
            d.setClassType(aClass);
            d.setIter(iter);
            d.setYearData(p.year());
            d = dataRepo.save(d);

            ParamsSet ps = new ParamsSet();
            ps.setData(d);
            // store raw parameters as a generic map, reusing BParamsMapper for JSON
            Map<String, Double> map = new HashMap<>();
            map.put("PNo", p.PNo());
            map.put("PNv", p.PNv());
            map.put("PNz", p.PNz());
            map.put("DIo", p.DIo());
            map.put("DIv", p.DIv());
            map.put("DIz", p.DIz());
            map.put("sumPoints", p.sumPoints());
            // we still need to persist as Object map for ParamsSet, so cast later
            Map<String, Object> objMap = new HashMap<>(map);
            ps.setParams(objMap);
            paramsRepo.save(ps);

            // compute using document service
            DocumentParamsDto dparams = new DocumentParamsDto(map);
            DocumentCalcDto dcalc = documentService.computeAll(dparams);

            ACalcDto calcDto = new ACalcDto(
                    null,
                    p.year(),
                    iter,
                    dcalc.get("PN"),
                    dcalc.get("DI"),
                    dcalc.get("KI"),
                    p.sumPoints() == null ? 0.0 : p.sumPoints(),
                    dcalc.get("TOTAL")
            );

            CalcResult cr = new CalcResult();
            cr.setData(d);
            cr.setCalcParams(ACalcMapper.toCalcJson(calcDto));
            calcRepo.save(cr);

            // we don't persist metric names for class A currently
            result.add(calcDto);
        }

        return result;
    }
}
