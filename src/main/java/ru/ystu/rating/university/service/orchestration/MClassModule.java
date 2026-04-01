package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;

@Component
public class MClassModule extends AbstractClassModule {

    public MClassModule(MClassCalculationHandler calculationHandler,
                        MClassReadModelProvider readModelProvider) {
        super(calculationHandler, readModelProvider);
    }

    @Override
    protected String classType() {
        return "M";
    }
}
