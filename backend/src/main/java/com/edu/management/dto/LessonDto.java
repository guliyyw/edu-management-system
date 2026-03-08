package com.edu.management.dto;

import com.edu.management.enums.LessonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class LessonDto {
    
    private Long id;
    private ClassDto classInfo;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String classroom;
    private LessonStatus status;
    private List<LessonAttendanceDto> attendances;
    private LocalDateTime createdAt;
}
