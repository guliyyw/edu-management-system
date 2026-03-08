package com.edu.management.repository;

import com.edu.management.entity.Campus;
import com.edu.management.enums.CampusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    
    List<Campus> findByStatus(CampusStatus status);
    
    boolean existsByName(String name);
}
