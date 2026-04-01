package ru.ystu.rating.university.service.orchestration;

import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;

public interface ClassReadModelProvider {

    String supportedClassType();

    HistoryResponseDto getHistory(Long userId);

    ClassParamsBlockDto getLastParams(Long userId);
}
