package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;

@Component
public class BClassModule extends AbstractClassModule {

    public BClassModule(BClassCalculationHandler calculationHandler,
                        BClassReadModelProvider readModelProvider) {
        super(calculationHandler, readModelProvider);
    }

    @Override
    protected String classType() {
        return "B";
    }
}
