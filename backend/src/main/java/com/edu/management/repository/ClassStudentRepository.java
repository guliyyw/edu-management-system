package com.edu.management.repository;

import com.edu.management.entity.ClassStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassStudentRepository extends JpaRepository<ClassStudent, Long> {
    
    List<ClassStudent> findByClassEntityId(Long classId);
    
    Optional<ClassStudent> findByClassEntityIdAndStudentId(Long classId, Long studentId);
    
    @Query("SELECT cs FROM ClassStudent cs WHERE cs.student.id = :studentId AND cs.isTrial = true")
    List<ClassStudent> findTrialClassesByStudentId(@Param("studentId") Long studentId);
    
    boolean existsByClassEntityIdAndStudentId(Long classId, Long studentId);

    void deleteByClassEntityId(Long classId);
}
