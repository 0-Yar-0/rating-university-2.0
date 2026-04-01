package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;

@Component
public class AClassModule extends AbstractClassModule {

    public AClassModule(AClassCalculationHandler calculationHandler,
                        AClassReadModelProvider readModelProvider) {
        super(calculationHandler, readModelProvider);
    }

    @Override
    protected String classType() {
        return "A";
    }
}
