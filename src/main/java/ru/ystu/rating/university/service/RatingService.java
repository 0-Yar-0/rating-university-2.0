package ru.ystu.rating.university.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ystu.rating.university.dto.*;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.model.UserIterState;
import ru.ystu.rating.university.repository.AppUserRepository;
import ru.ystu.rating.university.repository.DataRepository;
import ru.ystu.rating.university.repository.UserIterStateRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class RatingService {

    private final AppUserRepository userRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final BService bService;
    private final AService aService;
    // в будущем: VService

    public RatingService(AppUserRepository userRepo,
                         DataRepository dataRepo,
                         UserIterStateRepository userIterStateRepo,
                         BService bService,
                         AService aService) {
        this.userRepo = userRepo;
        this.dataRepo = dataRepo;
        this.userIterStateRepo = userIterStateRepo;
        this.bService = bService;
        this.aService = aService;
    }

    // ========================================================================
    // 1. СОХРАНЕНИЕ ПАРАМЕТРОВ ВСЕХ КЛАССОВ + РАСЧЁТ
    // ========================================================================

    /**
     * Оркестратор:
     * - считает next iteration для пользователя (глобально по всем классам);
     * - распределяет параметры по классам;
     * - делегирует расчёт для B в BService;
     * - для A и V заглушки.
     */
    @Transactional
    public MultiClassCalcResponseDto saveParamsAndComputeAll(
            Long userId,
            MultiClassParamsRequestDto request
    ) {
        AppUser user = userRepo.findById(userId).orElseThrow();

        int lastIter = dataRepo.findMaxIterForUser(user);
        int nextIter = lastIter + 1;

        List<ClassCalcBlockDto> resultBlocks = new ArrayList<>();

        if (request.classes() != null) {
            for (ClassParamsBlockDto block : request.classes()) {
                if (block == null || block.classType() == null) continue;

                String classType = block.classType().toUpperCase();

                switch (classType) {
                    case "B" -> {
                        @SuppressWarnings("unchecked")
                        List<BParamsDto> bParams = (List<BParamsDto>) (List<?>) block.data();
                        BMetricNamesDto names = block.names();

                        List<BCalcDto> bResults = bService.saveParamsAndComputeForB(
                                user,
                                nextIter,
                                bParams,
                                names
                        );
                        resultBlocks.add(new ClassCalcBlockDto("B", bResults));
                    }
                    case "A" -> {
                        @SuppressWarnings("unchecked")
                        List<AParamsDto> aParams = (List<AParamsDto>) (List<?>) block.data();
                        List<ACalcDto> aResults = aService.saveParamsAndComputeForA(
                                user,
                                nextIter,
                                aParams,
                                null
                        );
                        resultBlocks.add(new ClassCalcBlockDto("A", aResults));
                    }
                    case "V" -> {
                        // Заглушка VService
                        resultBlocks.add(new ClassCalcBlockDto("V", List.of()));
                    }
                    default -> {
                        // неизвестный класс — игнорируем
                    }
                }
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
     * Общая история по всем классам (A/B/V...) для пользователя.
     * Сейчас реально заполнен только B, остальные — заглушки.
     */
    @Transactional(readOnly = true)
    public MultiClassHistoryResponseDto getHistoryAllClasses(Long userId) {
        List<HistoryResponseDto> histories = new ArrayList<>();

        // histories.add(aService.getHistoryForA(userId));
        histories.add(bService.getHistoryForB(userId));
        // histories.add(vService.getHistoryForV(userId));

        return new MultiClassHistoryResponseDto(histories);
    }

    // ========================================================================
    // 3. ЭКСПОРТ ПОСЛЕДНИХ ПАРАМЕТРОВ (ПОКА ТОЛЬКО B)
    // ========================================================================

    /**
     * Экспорт "последнего состояния параметров" по всем классам.
     * Сейчас реально реализован только класс B:
     * - берём из BService последнюю итерацию для B;
     * - заворачиваем в MultiClassParamsRequestDto, чтобы фронт мог
     * сохранить это в JSON и потом импортировать (НА ФРОНТЕ).
     */
    @Transactional(readOnly = true)
    public MultiClassParamsRequestDto exportParams(Long userId) {
        List<ClassParamsBlockDto> blocks = new ArrayList<>();

        // Класс B
        ClassParamsBlockDto bBlock = bService.getLastParamsForB(userId);
        if (bBlock.data() != null && !bBlock.data().isEmpty()) {
            blocks.add(bBlock);
        }

        // В будущем сюда добавятся блоки A и V

        return new MultiClassParamsRequestDto(blocks);
    }

    @Transactional
    public void clearCurrentForUser(Long userId) {
        AppUser user = userRepo.findById(userId).orElseThrow();

        userIterStateRepo.findByAppUser(user).ifPresent(state -> {
            state.setCurrentIter(null);
            userIterStateRepo.save(state);
        });
    }

    @Transactional
    public void clearHistory(Long userId) {
        AppUser user = userRepo.findById(userId).orElseThrow();
        dataRepo.deleteAllByAppUser(user);
    }
}

