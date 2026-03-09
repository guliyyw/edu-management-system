package com.edu.management.repository;

import com.edu.management.entity.ClassEntity;
import com.edu.management.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<ClassEntity, Long> {

    List<ClassEntity> findByStatus(ClassStatus status);

    List<ClassEntity> findByTeacherIdAndStatus(Long teacherId, ClassStatus status);

    List<ClassEntity> findByCampusIdAndStatus(Long campusId, ClassStatus status);

    @Query("SELECT c FROM ClassEntity c JOIN c.students cs WHERE cs.student.id = :studentId AND c.status = :status")
    List<ClassEntity> findByStudentIdAndStatus(@Param("studentId") Long studentId, @Param("status") ClassStatus status);

    @Query("SELECT c FROM ClassEntity c WHERE c.teacher.id = :teacherId AND c.defaultDayOfWeek = :dayOfWeek AND c.status = :status")
    List<ClassEntity> findByTeacherIdAndDayOfWeekAndStatus(@Param("teacherId") Long teacherId,
                                                            @Param("dayOfWeek") Integer dayOfWeek,
                                                            @Param("status") ClassStatus status);

    @Query("SELECT DISTINCT c FROM ClassEntity c LEFT JOIN FETCH c.students cs LEFT JOIN FETCH cs.student WHERE c.id = :id")
    Optional<ClassEntity> findByIdWithStudents(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM ClassEntity c LEFT JOIN FETCH c.students cs LEFT JOIN FETCH cs.student WHERE c.status = :status")
    List<ClassEntity> findAllWithStudents(@Param("status") ClassStatus status);

    @Query("SELECT DISTINCT c FROM ClassEntity c LEFT JOIN FETCH c.students cs LEFT JOIN FETCH cs.student WHERE c.teacher.id = :teacherId AND c.status = :status")
    List<ClassEntity> findByTeacherIdAndStatusWithStudents(@Param("teacherId") Long teacherId, @Param("status") ClassStatus status);

    @Query("SELECT DISTINCT c FROM ClassEntity c LEFT JOIN FETCH c.students cs LEFT JOIN FETCH cs.student WHERE c.campus.id = :campusId AND c.status = :status")
    List<ClassEntity> findByCampusIdAndStatusWithStudents(@Param("campusId") Long campusId, @Param("status") ClassStatus status);
}
