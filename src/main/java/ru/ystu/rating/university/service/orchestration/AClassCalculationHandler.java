package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.service.AService;

import java.util.List;
import java.util.Map;

@Component
public class AClassCalculationHandler implements ClassCalculationHandler {

    private final AService aService;
    private final ClassBlockDataMapper blockDataMapper;

    public AClassCalculationHandler(AService aService, ClassBlockDataMapper blockDataMapper) {
        this.aService = aService;
        this.blockDataMapper = blockDataMapper;
    }

    @Override
    public String supportedClassType() {
        return "A";
    }

    @Override
    public ClassCalcBlockDto calculate(AppUser user, int iter, ClassParamsBlockDto block) {
        List<Map<String, Object>> params = blockDataMapper.toMapData(block, "A");

        List<Map<String, Object>> results = aService.saveParamsAndComputeForA(user, iter, params);
        return new ClassCalcBlockDto("A", results);
    }
}
