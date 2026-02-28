package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ystu.rating.university.model.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);
}
