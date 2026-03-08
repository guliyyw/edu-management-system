package com.edu.management.repository;

import com.edu.management.entity.Teacher;
import com.edu.management.enums.TeacherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    List<Teacher> findByStatus(TeacherStatus status);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT t FROM Teacher t JOIN t.campuses c WHERE c.id = :campusId AND t.status = :status")
    List<Teacher> findByCampusIdAndStatus(@Param("campusId") Long campusId, @Param("status") TeacherStatus status);
}
