package com.edu.management.repository;

import com.edu.management.entity.Lesson;
import com.edu.management.enums.LessonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findByClassEntityId(Long classId);
    
    List<Lesson> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT l FROM Lesson l WHERE l.classEntity.teacher.id = :teacherId AND l.date = :date")
    List<Lesson> findByTeacherIdAndDate(@Param("teacherId") Long teacherId, @Param("date") LocalDate date);
    
    @Query("SELECT l FROM Lesson l WHERE l.classEntity.teacher.id = :teacherId AND l.date BETWEEN :startDate AND :endDate")
    List<Lesson> findByTeacherIdAndDateBetween(@Param("teacherId") Long teacherId, 
                                                @Param("startDate") LocalDate startDate, 
                                                @Param("endDate") LocalDate endDate);
    
    @Query("SELECT l FROM Lesson l WHERE l.classEntity.campus.id = :campusId AND l.date = :date")
    List<Lesson> findByCampusIdAndDate(@Param("campusId") Long campusId, @Param("date") LocalDate date);
    
    List<Lesson> findByStatus(LessonStatus status);
    
    @Query("SELECT l FROM Lesson l WHERE l.classEntity.id = :classId AND l.date = :date")
    List<Lesson> findByClassIdAndDate(@Param("classId") Long classId, @Param("date") LocalDate date);
}
