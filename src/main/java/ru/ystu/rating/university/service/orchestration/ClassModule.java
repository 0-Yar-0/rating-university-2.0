package ru.ystu.rating.university.service.orchestration;

import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.model.AppUser;

public interface ClassModule {

    String supportedClassType();

    ClassCalcBlockDto calculate(AppUser user, int iter, ClassParamsBlockDto block);

    HistoryResponseDto getHistory(Long userId);

    ClassParamsBlockDto getLastParams(Long userId);
}
