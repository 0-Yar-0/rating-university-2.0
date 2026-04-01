package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.service.MService;

import java.util.List;
import java.util.Map;

@Component
public class MClassCalculationHandler implements ClassCalculationHandler {

    private final MService mService;
    private final ClassBlockDataMapper blockDataMapper;

    public MClassCalculationHandler(MService mService, ClassBlockDataMapper blockDataMapper) {
        this.mService = mService;
        this.blockDataMapper = blockDataMapper;
    }

    @Override
    public String supportedClassType() {
        return "M";
    }

    @Override
    public ClassCalcBlockDto calculate(AppUser user, int iter, ClassParamsBlockDto block) {
        List<Map<String, Object>> params = blockDataMapper.toMapData(block, "M");

        List<Map<String, Object>> results = mService.saveParamsAndComputeForM(user, iter, params);
        return new ClassCalcBlockDto("M", results);
    }
}
