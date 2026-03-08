package com.edu.management.repository;

import com.edu.management.entity.Student;
import com.edu.management.enums.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    List<Student> findByStatus(StudentStatus status);
    
    boolean existsByParentPhone(String parentPhone);
    
    @Query("SELECT s FROM Student s WHERE s.name LIKE %:keyword% OR s.parentPhone LIKE %:keyword%")
    List<Student> searchByKeyword(@Param("keyword") String keyword);
}
