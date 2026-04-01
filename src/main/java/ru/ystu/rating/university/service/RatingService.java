package ru.ystu.rating.university.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ystu.rating.university.dto.*;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.model.UserIterState;
import ru.ystu.rating.university.repository.AppUserRepository;
import ru.ystu.rating.university.repository.DataRepository;
import ru.ystu.rating.university.repository.UserIterStateRepository;
import ru.ystu.rating.university.service.orchestration.ClassModule;
import ru.ystu.rating.university.service.orchestration.ClassModuleFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class RatingService {

    private final AppUserRepository userRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final ClassModuleFactory classModuleFactory;

    public RatingService(AppUserRepository userRepo,
                         DataRepository dataRepo,
                         UserIterStateRepository userIterStateRepo,
                         ClassModuleFactory classModuleFactory) {
        this.userRepo = userRepo;
        this.dataRepo = dataRepo;
        this.userIterStateRepo = userIterStateRepo;
        this.classModuleFactory = classModuleFactory;
    }

    // ========================================================================
    // 1. СОХРАНЕНИЕ ПАРАМЕТРОВ ВСЕХ КЛАССОВ + РАСЧЁТ
    // ========================================================================

    /**
     * Оркестратор:
     * - считает next iteration для пользователя (глобально по всем классам);
     * - распределяет параметры по классам;
    * - делегирует расчет соответствующему обработчику класса.
     */
    @Transactional
    public MultiClassCalcResponseDto saveParamsAndComputeAll(
            Long userId,
            MultiClassParamsRequestDto request
    ) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();

        int lastIter = dataRepo.findMaxIterForUser(user);
        int nextIter = lastIter + 1;

        List<ClassCalcBlockDto> resultBlocks = new ArrayList<>();

        if (request.classes() != null) {
            for (ClassParamsBlockDto block : request.classes()) {
                if (block == null) continue;

                ClassModule module = classModuleFactory.getOrNoOp(block.classType());

                resultBlocks.add(module.calculate(user, nextIter, block));
            }
        }

        UserIterState state = userIterStateRepo
                .findByAppUser(user)
                .orElseGet(() -> {
                    UserIterState s = new UserIterState();
                    s.setAppUser(user);
                    return s;
                });
        state.setCurrentIter(nextIter);
        userIterStateRepo.save(state);

        return new MultiClassCalcResponseDto(resultBlocks);
    }

    // ========================================================================
    // 2. ИСТОРИЯ ПО ВСЕМ КЛАССАМ
    // ========================================================================

    /**
    * Общая история по всем классам (A/B/M...) для пользователя.
     * Сейчас реально заполнен только B, остальные — заглушки.
     */
    @Transactional(readOnly = true)
    public MultiClassHistoryResponseDto getHistoryAllClasses(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        List<HistoryResponseDto> histories = classModuleFactory.getSortedModules().stream()
            .map(module -> module.getHistory(safeUserId))
                .toList();

        return new MultiClassHistoryResponseDto(histories);
    }

    // ========================================================================
    // 3. ЭКСПОРТ ПОСЛЕДНИХ ПАРАМЕТРОВ
    // ========================================================================

    /**
     * Экспорт "последнего состояния параметров" по всем классам.
        * Данные возвращаются блоками по классам A/B/M.
     */
    @Transactional(readOnly = true)
    public MultiClassParamsRequestDto exportParams(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        List<ClassParamsBlockDto> blocks = classModuleFactory.getSortedModules().stream()
            .map(module -> module.getLastParams(safeUserId))
                .filter(block -> block.data() != null && !block.data().isEmpty())
                .toList();

        return new MultiClassParamsRequestDto(blocks);
    }

    @Transactional
    public void clearCurrentForUser(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();

        userIterStateRepo.findByAppUser(user).ifPresent(state -> {
            state.setCurrentIter(null);
            userIterStateRepo.save(state);
        });
    }

    @Transactional
    public void clearHistory(Long userId) {
        Long safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        AppUser user = userRepo.findById(safeUserId).orElseThrow();
        dataRepo.deleteAllByAppUser(user);
    }
}

