package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.service.AService;

@Component
public class AClassReadModelProvider implements ClassReadModelProvider {

    private final AService aService;

    public AClassReadModelProvider(AService aService) {
        this.aService = aService;
    }

    @Override
    public String supportedClassType() {
        return "A";
    }

    @Override
    public HistoryResponseDto getHistory(Long userId) {
        return aService.getHistoryForA(userId);
    }

    @Override
    public ClassParamsBlockDto getLastParams(Long userId) {
        return aService.getLastParamsForA(userId);
    }
}
