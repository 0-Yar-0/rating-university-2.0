package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.service.BService;

@Component
public class BClassReadModelProvider implements ClassReadModelProvider {

    private final BService bService;

    public BClassReadModelProvider(BService bService) {
        this.bService = bService;
    }

    @Override
    public String supportedClassType() {
        return "B";
    }

    @Override
    public HistoryResponseDto getHistory(Long userId) {
        return bService.getHistoryForB(userId);
    }

    @Override
    public ClassParamsBlockDto getLastParams(Long userId) {
        return bService.getLastParamsForB(userId);
    }
}
