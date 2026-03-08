package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ClassStudentDto {
    
    private Long id;
    private StudentDto student;
    private Boolean isTrial;
    private LocalDateTime convertedAt;
    private LocalDateTime createdAt;
}
