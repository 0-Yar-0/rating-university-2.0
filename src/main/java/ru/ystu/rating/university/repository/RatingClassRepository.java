package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ystu.rating.university.model.RatingClass;

import java.util.Optional;

public interface RatingClassRepository extends JpaRepository<RatingClass, Long> {

    Optional<RatingClass> findByCode(String code);
}
