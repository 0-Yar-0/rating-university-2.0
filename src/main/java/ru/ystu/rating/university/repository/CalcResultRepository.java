package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ystu.rating.university.model.CalcResult;

import java.util.Optional;

public interface CalcResultRepository extends JpaRepository<CalcResult, Long> {
    Optional<CalcResult> findByDataId(Long dataId);

    void deleteByDataId(Long dataId);
}
