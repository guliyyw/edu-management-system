package com.edu.management.repository;

import com.edu.management.entity.User;
import com.edu.management.enums.RoleType;
import com.edu.management.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUsernameAndStatus(String username, UserStatus status);
    
    boolean existsByUsername(String username);
    
    List<User> findByRoleAndStatus(RoleType role, UserStatus status);
    
    List<User> findByStatus(UserStatus status);
}
