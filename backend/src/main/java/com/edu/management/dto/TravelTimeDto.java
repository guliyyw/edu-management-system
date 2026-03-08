package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TravelTimeDto {
    
    private Long id;
    private TeacherDto teacher;
    private CampusDto fromCampus;
    private CampusDto toCampus;
    private Integer travelMinutes;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt;
}
