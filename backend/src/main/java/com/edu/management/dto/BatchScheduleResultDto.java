package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BatchScheduleResultDto {

    private int totalCount;
    private int successCount;
    private int failCount;
    private List<LessonDto> successLessons;
    private List<BatchScheduleRecordDto> failRecords;
}
