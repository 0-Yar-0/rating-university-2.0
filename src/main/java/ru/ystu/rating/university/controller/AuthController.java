package ru.ystu.rating.university.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import ru.ystu.rating.university.dto.AuthResponse;
import ru.ystu.rating.university.dto.LoginRequest;
import ru.ystu.rating.university.dto.RegisterRequest;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.repository.AppUserRepository;
import ru.ystu.rating.university.security.CustomUserDetails;
import ru.ystu.rating.university.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Регистрация и авторизация через сессии")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository securityContextRepository;
    private final AppUserRepository userRepo;

    public AuthController(AuthService authService,
                          AuthenticationManager authManager,
                          SecurityContextRepository securityContextRepository,
                          AppUserRepository userRepo) {
        this.authService = authService;
        this.authManager = authManager;
        this.securityContextRepository = securityContextRepository;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public AuthResponse register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthResponse resp = authService.register(req);

        var authToken = new UsernamePasswordAuthenticationToken(req.email(), req.password());
        var auth = authManager.authenticate(authToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return resp;
    }

    @PostMapping("/login")
    @Operation(summary = "Логин, создаёт сессию и cookie JSESSIONID")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        var authToken = new UsernamePasswordAuthenticationToken(req.email(), req.password());
        var auth = authManager.authenticate(authToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        AppUser user = userRepo.findByEmail(req.email())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return authService.toAuthResponse(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Выход, очищает сессию и SecurityContext")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @GetMapping("/me")
    @Operation(summary = "Информация о текущем пользователе")
    public AuthResponse me(@AuthenticationPrincipal CustomUserDetails principal) {
        if (principal == null) {
            throw new RuntimeException("Not authenticated");
        }

        AppUser user = userRepo.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "User not found by id: " + principal.getId()
                ));

        return authService.toAuthResponse(user);
    }
}
