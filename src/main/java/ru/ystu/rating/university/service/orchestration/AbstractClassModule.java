package ru.ystu.rating.university.service.orchestration;

import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.model.AppUser;

public abstract class AbstractClassModule implements ClassModule {

    private final ClassCalculationHandler calculationHandler;
    private final ClassReadModelProvider readModelProvider;

    protected AbstractClassModule(ClassCalculationHandler calculationHandler,
                                  ClassReadModelProvider readModelProvider) {
        this.calculationHandler = calculationHandler;
        this.readModelProvider = readModelProvider;
    }

    @Override
    public final String supportedClassType() {
        return classType();
    }

    @Override
    public final ClassCalcBlockDto calculate(AppUser user, int iter, ClassParamsBlockDto block) {
        return calculationHandler.calculate(user, iter, block);
    }

    @Override
    public final HistoryResponseDto getHistory(Long userId) {
        return readModelProvider.getHistory(userId);
    }

    @Override
    public final ClassParamsBlockDto getLastParams(Long userId) {
        return readModelProvider.getLastParams(userId);
    }

    protected abstract String classType();
}
