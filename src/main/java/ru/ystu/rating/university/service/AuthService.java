package ru.ystu.rating.university.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ystu.rating.university.dto.AuthResponse;
import ru.ystu.rating.university.dto.RegisterRequest;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.model.Role;
import ru.ystu.rating.university.repository.AppUserRepository;
import ru.ystu.rating.university.repository.RoleRepository;

import java.time.OffsetDateTime;

@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    public AuthService(AppUserRepository userRepo,
                       RoleRepository roleRepo,
                       PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.encoder = encoder;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepo.findByEmail(req.email()).isPresent()) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        Role userRole = roleRepo.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Роль USER не найдена"));

        AppUser u = new AppUser();
        u.setName(req.name());
        u.setEmail(req.email());
        u.setPasswordHash(encoder.encode(req.password()));
        u.setRole(userRole);
        u.setLastActive(OffsetDateTime.now());

        u = userRepo.save(u);

        return new AuthResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().getName()
        );
    }

    public AuthResponse toAuthResponse(AppUser user) {
        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().getName()
        );
    }
}

