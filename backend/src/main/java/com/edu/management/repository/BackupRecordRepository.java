package com.edu.management.repository;

import com.edu.management.entity.BackupRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BackupRecordRepository extends JpaRepository<BackupRecord, Long> {

    List<BackupRecord> findByStatusOrderByCreatedAtDesc(String status);

    List<BackupRecord> findAllByOrderByCreatedAtDesc();
}
