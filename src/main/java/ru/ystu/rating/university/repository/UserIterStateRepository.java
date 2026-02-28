package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.model.UserIterState;

import java.util.Optional;

public interface UserIterStateRepository extends JpaRepository<UserIterState, Long> {

    Optional<UserIterState> findByAppUser(AppUser user);
}

