package ru.ystu.rating.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.ystu.rating.university.dto.MultiClassCalcResponseDto;
import ru.ystu.rating.university.dto.MultiClassHistoryResponseDto;
import ru.ystu.rating.university.dto.MultiClassParamsRequestDto;
import ru.ystu.rating.university.security.CustomUserDetails;
import ru.ystu.rating.university.service.RatingService;

@RestController
@RequestMapping("/api/rating")
@Tag(
        name = "Rating",
        description = "Параметры и расчеты по всем классам (A/B/V): сохранение параметров и расчёты, история"
)
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    /**
     * Создать новую итерацию для пользователя, сохранить параметры по
     * всем переданным классам (A/B/V) и выполнить расчёт.
     * <p>
     * Сейчас реально считается только класс B — A и V заглушки.
     * <p>
     * userId - SecurityContext
     */
    @PostMapping("/calc-multi")
    @Operation(
            summary = "Сохранить параметры по классам A/B/V и пересчитать (новая итерация)",
            description = "Сейчас расчёт выполняется только для класса B; A и V — заглушки"
    )
    public MultiClassCalcResponseDto saveAndComputeAll(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestBody MultiClassParamsRequestDto request
    ) {
        Long userId = principal.getId();
        return ratingService.saveParamsAndComputeAll(userId, request);
    }

    /**
     * Получение истории по всем итерациям и классам (A/B/V) для
     * конкретного пользователя
     */
    @GetMapping("/history")
    @Operation(summary = "История по всем классам A/B/V для пользователя")
    public MultiClassHistoryResponseDto getFullHistory(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = principal.getId();
        return ratingService.getHistoryAllClasses(userId);
    }

    /**
     * Экспорт последнего состояния параметров по всем классам.
     * Сейчас реально экспортируются только параметры класса B
     * из последней итерации пользователя.
     */
    @GetMapping("/export-params")
    @Operation(
            summary = "Экспорт последних параметров по всем классам",
            description = "Возвращает структуру MultiClassParamsRequestDto; " +
                    "пока реализован только класс B"
    )
    public MultiClassParamsRequestDto exportParams(
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        Long userId = principal.getId();
        return ratingService.exportParams(userId);
    }

    @PostMapping("/clear-current")
    @Operation(summary = "Очистить текущие данные пользователя по всем классам (но не удалять историю)")
    public void clearCurrent(@AuthenticationPrincipal CustomUserDetails principal) {
        ratingService.clearCurrentForUser(principal.getId());
    }

    @DeleteMapping("/history")
    @Operation(summary = "Удалить всю историю расчётов пользователя")
    public void clearHistory(@AuthenticationPrincipal CustomUserDetails principal) {
        Long userId = principal.getId();
        ratingService.clearHistory(userId);
    }

}

