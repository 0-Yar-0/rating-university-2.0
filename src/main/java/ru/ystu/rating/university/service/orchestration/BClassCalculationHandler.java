package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.BCalcDto;
import ru.ystu.rating.university.dto.BMetricNamesDto;
import ru.ystu.rating.university.dto.BParamsDto;
import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.service.BService;

import java.util.List;
import java.util.Map;

@Component
public class BClassCalculationHandler implements ClassCalculationHandler {

    private final BService bService;
    private final ClassBlockDataMapper blockDataMapper;

    public BClassCalculationHandler(BService bService, ClassBlockDataMapper blockDataMapper) {
        this.bService = bService;
        this.blockDataMapper = blockDataMapper;
    }

    @Override
    public String supportedClassType() {
        return "B";
    }

    @Override
    public ClassCalcBlockDto calculate(AppUser user, int iter, ClassParamsBlockDto block) {
        List<Map<String, Object>> rawParams = blockDataMapper.toRawMapData(block, "B");
        List<BParamsDto> params = blockDataMapper.toTypedData(block, BParamsDto.class, "B");

        BMetricNamesDto names = block.names();
        List<BCalcDto> results = bService.saveParamsAndComputeForB(user, iter, params, rawParams, names);
        return new ClassCalcBlockDto("B", results);
    }
}
