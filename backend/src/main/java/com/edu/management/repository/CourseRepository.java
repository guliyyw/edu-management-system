package com.edu.management.repository;

import com.edu.management.entity.Course;
import com.edu.management.enums.CourseStatus;
import com.edu.management.enums.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByStatus(CourseStatus status);
    
    List<Course> findByTypeAndStatus(CourseType type, CourseStatus status);
}
