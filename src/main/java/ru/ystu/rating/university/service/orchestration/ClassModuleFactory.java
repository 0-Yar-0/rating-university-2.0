package ru.ystu.rating.university.service.orchestration;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ClassModuleFactory {

    private final Map<String, ClassModule> modulesByType;
    private final ClassModule unknownModule = new UnknownClassModule();

    public ClassModuleFactory(List<ClassModule> modules) {
        this.modulesByType = modules.stream()
                .collect(Collectors.toMap(
                        m -> m.supportedClassType().toUpperCase(),
                        Function.identity(),
                        (existing, replacement) -> {
                            throw new IllegalStateException("Duplicate module for class type "
                                    + existing.supportedClassType());
                        }
                ));
    }

    public ClassModule getOrNoOp(String classType) {
        if (classType == null) {
            return unknownModule;
        }
        return modulesByType.getOrDefault(classType.toUpperCase(), unknownModule);
    }

    public List<ClassModule> getSortedModules() {
        return modulesByType.values().stream()
                .sorted(Comparator.comparing(ClassModule::supportedClassType))
                .toList();
    }
}
