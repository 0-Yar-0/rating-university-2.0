package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ystu.rating.university.model.CalcResultName;

import java.util.Optional;

public interface CalcResultNameRepository extends JpaRepository<CalcResultName, Long> {

    Optional<CalcResultName> findByCalcResultId(Long calcResultId);

    void deleteByCalcResultId(Long calcResultId);
}
