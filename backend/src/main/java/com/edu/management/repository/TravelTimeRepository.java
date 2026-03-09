package com.edu.management.repository;

import com.edu.management.entity.TravelTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TravelTimeRepository extends JpaRepository<TravelTime, Long> {

    @Query("SELECT t FROM TravelTime t LEFT JOIN FETCH t.teacher LEFT JOIN FETCH t.fromCampus LEFT JOIN FETCH t.toCampus")
    List<TravelTime> findAllWithDetails();

    @Query("SELECT t FROM TravelTime t LEFT JOIN FETCH t.teacher LEFT JOIN FETCH t.fromCampus LEFT JOIN FETCH t.toCampus WHERE t.teacher.id = :teacherId")
    List<TravelTime> findByTeacherId(@Param("teacherId") Long teacherId);
    
    @Query("SELECT t FROM TravelTime t WHERE t.teacher.id = :teacherId AND t.fromCampus.id = :fromCampusId AND t.toCampus.id = :toCampusId AND t.effectiveDate <= :date ORDER BY t.effectiveDate DESC")
    Optional<TravelTime> findEffectiveTravelTime(@Param("teacherId") Long teacherId, 
                                                  @Param("fromCampusId") Long fromCampusId, 
                                                  @Param("toCampusId") Long toCampusId, 
                                                  @Param("date") LocalDate date);
    
    @Query("SELECT t FROM TravelTime t WHERE t.teacher.id = :teacherId AND t.effectiveDate <= :date ORDER BY t.effectiveDate DESC")
    List<TravelTime> findByTeacherIdAndEffectiveDate(@Param("teacherId") Long teacherId, @Param("date") LocalDate date);
    
    boolean existsByTeacherIdAndFromCampusIdAndToCampusIdAndEffectiveDate(Long teacherId, Long fromCampusId, Long toCampusId, LocalDate effectiveDate);
}
