package ru.ystu.rating.university.dto;

public record AuthResponse(
        Long userId,
        String name,
        String email,
        String role
) {
}
