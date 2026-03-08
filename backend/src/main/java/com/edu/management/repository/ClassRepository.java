package com.edu.management.repository;

import com.edu.management.entity.ClassEntity;
import com.edu.management.enums.ClassStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
}
