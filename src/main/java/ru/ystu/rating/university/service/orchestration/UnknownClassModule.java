package ru.ystu.rating.university.service.orchestration;

import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.model.AppUser;

import java.util.List;

final class UnknownClassModule implements ClassModule {

    @Override
    public String supportedClassType() {
        return "UNKNOWN";
    }

    @Override
    public ClassCalcBlockDto calculate(AppUser user, int iter, ClassParamsBlockDto block) {
        String classType = block == null ? "UNKNOWN" : String.valueOf(block.classType());
        return new ClassCalcBlockDto(classType, List.of());
    }

    @Override
    public HistoryResponseDto getHistory(Long userId) {
        return new HistoryResponseDto("UNKNOWN", List.of());
    }

    @Override
    public ClassParamsBlockDto getLastParams(Long userId) {
        return new ClassParamsBlockDto("UNKNOWN", List.of(), null);
    }
}
