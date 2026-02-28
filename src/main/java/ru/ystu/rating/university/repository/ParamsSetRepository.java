package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.ystu.rating.university.model.ParamsSet;

import java.util.List;
import java.util.Optional;

public interface ParamsSetRepository extends JpaRepository<ParamsSet, Long> {

    Optional<ParamsSet> findByDataId(Long dataId);

    void deleteByDataId(Long dataId);
}
