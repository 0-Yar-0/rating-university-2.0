package ru.ystu.rating.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.ystu.rating.university.model.AppUser;
import ru.ystu.rating.university.model.Data;
import ru.ystu.rating.university.model.RatingClass;

import java.util.List;

public interface DataRepository extends JpaRepository<Data, Long> {

    /**
     * Найти максимальный номер итерации для пользователя (глобально по всем классам и годам).
     */
    @Query("""
                SELECT COALESCE(MAX(d.iter), 0)
                FROM Data d
                WHERE d.appUser = :user
            """)
    int findMaxIterForUser(@Param("user") AppUser user);

    /**
     * Найти максимальный номер итерации для данного пользователя и класса
     */
    @Query("""
                SELECT COALESCE(MAX(d.iter), 0)
                FROM Data d
                WHERE d.appUser = :user
                  AND d.classType = :classType
            """)
    Integer findMaxIterForUserAndClass(@Param("user") AppUser user,
                                       @Param("classType") RatingClass classType);

    /**
     * Получить ВСЕ записи определённой итерации и определённого класса для пользователя
     */
    List<Data> findAllByAppUserAndClassTypeAndIterOrderByYearDataAsc(
            AppUser appUser,
            RatingClass classType,
            Integer iter
    );

    /**
     * 2) Получить ВСЕ записи определённой итерации пользователя,
     * отсортированные по классу и году.
     */
    List<Data> findAllByAppUserAndIterOrderByClassTypeIdAscYearDataAsc(
            AppUser appUser,
            Integer iter
    );

    /**
     * 3) Получить ВСЕ записи пользователя по всем итерациям,
     * отсортированные по номеру итерации, затем по классу, затем по году.
     */
    List<Data> findAllByAppUserOrderByIterAscClassTypeIdAscYearDataAsc(AppUser appUser);

    void deleteAllByAppUser(AppUser appUser);

    /**
     * Remove specific record when re-saving same year/iter to avoid unique constraint
     */
    void deleteByAppUserAndClassTypeAndIterAndYearData(
            AppUser appUser,
            RatingClass classType,
            Integer iter,
            Integer yearData
    );
}

