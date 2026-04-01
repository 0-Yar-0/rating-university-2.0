package ru.ystu.rating.university.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class RatingService {

    private final AppUserRepository userRepo;
    private final DataRepository dataRepo;
    private final UserIterStateRepository userIterStateRepo;
    private final ClassModuleFactory classModuleFactory;
        private final ObjectMapper objectMapper;

        private static final String[] A_SHARED_FROM_B = {
            "PNo", "PNv", "PNz", "DIo", "DIv", "DIz",
            "WL2022", "WL2023", "WL2024",
            "NPR2022", "NPR2023", "NPR2024",
            "DN2022", "DN2023", "DN2024",
            "OD2022", "OD2023", "OD2024"
        };

        private static final String[] M_SHARED_FROM_B = {
            "N", "Npr", "VO", "PO", "NR2023", "NR2024", "NR2025"
        };

    public RatingService(AppUserRepository userRepo,
                         DataRepository dataRepo,
                         UserIterStateRepository userIterStateRepo,
                 ClassModuleFactory classModuleFactory,
                 ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.dataRepo = dataRepo;
        this.userIterStateRepo = userIterStateRepo;
        this.classModuleFactory = classModuleFactory;
        this.objectMapper = objectMapper;
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

        List<ClassParamsBlockDto> normalizedBlocks = enrichWithSharedBMetrics(request.classes());
        if (normalizedBlocks != null) {
            for (ClassParamsBlockDto block : normalizedBlocks) {
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

    private List<ClassParamsBlockDto> enrichWithSharedBMetrics(List<ClassParamsBlockDto> blocks) {
        if (blocks == null || blocks.isEmpty()) {
            return blocks;
        }

        ClassParamsBlockDto bBlock = blocks.stream()
                .filter(Objects::nonNull)
                .filter(b -> "B".equalsIgnoreCase(b.classType()))
                .findFirst()
                .orElse(null);
        if (bBlock == null || bBlock.data() == null || bBlock.data().isEmpty()) {
            return blocks;
        }

        Map<Integer, Map<String, Object>> bByYear = indexByYear(toMapData(bBlock.data()));
        if (bByYear.isEmpty()) {
            return blocks;
        }

        List<ClassParamsBlockDto> out = new ArrayList<>(blocks.size());
        for (ClassParamsBlockDto block : blocks) {
            if (block == null || block.data() == null || block.data().isEmpty()) {
                out.add(block);
                continue;
            }

            String classType = block.classType() == null ? "" : block.classType().toUpperCase(Locale.ROOT);
            if (!"A".equals(classType) && !"M".equals(classType)) {
                out.add(block);
                continue;
            }

            List<Map<String, Object>> rows = toMapData(block.data());
            String[] sharedKeys = "A".equals(classType) ? A_SHARED_FROM_B : M_SHARED_FROM_B;
            for (Map<String, Object> row : rows) {
                Integer year = toIntOrNull(row.get("year"));
                if (year == null) {
                    continue;
                }
                Map<String, Object> bRow = bByYear.get(year);
                if (bRow == null) {
                    continue;
                }
                for (String key : sharedKeys) {
                    Object current = row.get(key);
                    if (current != null) {
                        continue;
                    }
                    Object fallback = bRow.get(key);
                    if (fallback != null) {
                        row.put(key, fallback);
                    }
                }
            }

            out.add(new ClassParamsBlockDto(block.classType(), rows, block.names()));
        }

        return out;
    }

    private List<Map<String, Object>> toMapData(List<?> data) {
        JavaType targetType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, Map.class);
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> mapped = (List<Map<String, Object>>) objectMapper.convertValue(data, targetType);
            return mapped;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private Map<Integer, Map<String, Object>> indexByYear(List<Map<String, Object>> rows) {
        Map<Integer, Map<String, Object>> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Integer year = toIntOrNull(row.get("year"));
            if (year != null) {
                out.put(year, row);
            }
        }
        return out;
    }

    private static Integer toIntOrNull(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        String s = value.toString().trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return null;
        }
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

