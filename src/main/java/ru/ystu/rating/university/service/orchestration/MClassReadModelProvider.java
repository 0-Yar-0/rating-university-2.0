package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.service.MService;

@Component
public class MClassReadModelProvider implements ClassReadModelProvider {

    private final MService mService;

    public MClassReadModelProvider(MService mService) {
        this.mService = mService;
    }

    @Override
    public String supportedClassType() {
        return "M";
    }

    @Override
    public HistoryResponseDto getHistory(Long userId) {
        return mService.getHistoryForM(userId);
    }

    @Override
    public ClassParamsBlockDto getLastParams(Long userId) {
        return mService.getLastParamsForM(userId);
    }
}
