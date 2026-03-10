package com.edu.management.repository;

import com.edu.management.entity.BackupConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BackupConfigRepository extends JpaRepository<BackupConfig, Long> {

    Optional<BackupConfig> findFirstByOrderByIdAsc();
}
