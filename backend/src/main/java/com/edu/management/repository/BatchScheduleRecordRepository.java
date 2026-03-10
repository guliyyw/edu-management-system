package com.edu.management.repository;

import com.edu.management.entity.BatchScheduleRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchScheduleRecordRepository extends JpaRepository<BatchScheduleRecord, Long> {

    List<BatchScheduleRecord> findByClassEntityIdAndScheduleDateBetween(Long classId, LocalDate startDate, LocalDate endDate);

    List<BatchScheduleRecord> findByClassEntityIdAndScheduleDate(Long classId, LocalDate scheduleDate);

    Optional<BatchScheduleRecord> findByClassEntityIdAndScheduleDateAndStatus(Long classId, LocalDate scheduleDate, String status);

    List<BatchScheduleRecord> findByClassEntityIdAndStatus(Long classId, String status);

    List<BatchScheduleRecord> findByStatus(String status);
}
