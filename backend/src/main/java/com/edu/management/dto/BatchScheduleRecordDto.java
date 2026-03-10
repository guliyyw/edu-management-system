package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BatchScheduleRecordDto {
    private Long id;
    private Long classId;
    private String className;
    private LocalDate scheduleDate;
    private String status;
    private String failReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
