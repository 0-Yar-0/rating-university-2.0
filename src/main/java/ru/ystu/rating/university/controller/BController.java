package ru.ystu.rating.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.ystu.rating.university.dto.ClassCalcBlockDto;
import ru.ystu.rating.university.dto.ClassParamsBlockDto;
import ru.ystu.rating.university.dto.HistoryResponseDto;
import ru.ystu.rating.university.dto.MetricNamesDto;
import ru.ystu.rating.university.security.CustomUserDetails;
import ru.ystu.rating.university.service.BService;

@RestController
@RequestMapping("/api/b")
@Tag(
        name = "Class B",
        description = "Параметры и расчёты класса B (итерации, пользователи)"
)
public class BController {

    private final BService bService;

    public BController(BService bService) {
        this.bService = bService;
    }

    /**
     * Получить последние введённые параметры класса B
     * (последняя итерация для данного пользователя).
     * <p>
     * userId - SecurityContext,
     */
    @GetMapping("/params/last")
    @Operation(summary = "Получить последние введённые параметры класса B (последняя итерация)")
    public ClassParamsBlockDto getLastParamsForB(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = principal.getId();
        return bService.getLastParamsForB(userId);
    }

    /**
     * Получить рассчитанные значения по классу B за последнюю итерацию пользователя.
     */
    @GetMapping("/calc/last")
    @Operation(summary = "Получить последние расчёты класса B (последняя итерация)")
    public ClassCalcBlockDto getLastCalcForB(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = principal.getId();
        return bService.getLastCalcForB(userId);
    }

    /**
     * История по классу B: все итерации пользователя.
     */
    @GetMapping("/history")
    @Operation(summary = "Получить историю расчётов класса B (все итерации)")
    public HistoryResponseDto getHistoryForB(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = principal.getId();
        return bService.getHistoryForB(userId);
    }

    /**
     * Обновление названий метрик для одного результата (поддерживает B11..B44).
     */
    @PutMapping("/metric-names")
    @Operation(summary = "Обновить названия метрик (поддерживает B11..B44) для конкретного результата")
    public void updateMetricNames(@RequestBody MetricNamesDto dto) {
        bService.updateMetricNames(dto);
    }

    @GetMapping("/params/iter/{iter}")
    @Operation(summary = "Получить введённые параметры класса B для заданной итерации")
    public ClassParamsBlockDto getParamsForBIter(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable("iter") Integer iter
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Long userId = principal.getId();
        return bService.getParamsForBIter(userId, iter);
    }

}
