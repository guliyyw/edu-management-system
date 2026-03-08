package com.edu.management.repository;

import com.edu.management.entity.LessonAttendance;
import com.edu.management.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonAttendanceRepository extends JpaRepository<LessonAttendance, Long> {
    
    List<LessonAttendance> findByLessonId(Long lessonId);
    
    Optional<LessonAttendance> findByLessonIdAndStudentId(Long lessonId, Long studentId);
    
    @Query("SELECT la FROM LessonAttendance la WHERE la.lesson.classEntity.teacher.id = :teacherId AND la.lesson.date BETWEEN :startDate AND :endDate")
    List<LessonAttendance> findByTeacherIdAndDateBetween(@Param("teacherId") Long teacherId, 
                                                          @Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT la FROM LessonAttendance la WHERE la.student.id = :studentId AND la.lesson.date BETWEEN :startDate AND :endDate")
    List<LessonAttendance> findByStudentIdAndDateBetween(@Param("studentId") Long studentId, 
                                                          @Param("startDate") LocalDate startDate, 
                                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT la FROM LessonAttendance la WHERE la.lesson.classEntity.campus.id = :campusId AND la.lesson.date BETWEEN :startDate AND :endDate")
    List<LessonAttendance> findByCampusIdAndDateBetween(@Param("campusId") Long campusId, 
                                                         @Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);
    
    List<LessonAttendance> findByStatus(AttendanceStatus status);
    
    @Query("SELECT COUNT(la) FROM LessonAttendance la WHERE la.status = :status AND la.lesson.classEntity.teacher.id = :teacherId AND la.lesson.date BETWEEN :startDate AND :endDate")
    Long countByStatusAndTeacherIdAndDateBetween(@Param("status") AttendanceStatus status, 
                                                  @Param("teacherId") Long teacherId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
}
