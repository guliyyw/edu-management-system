package com.edu.management.dto;

import com.edu.management.enums.StudentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StudentDto {
    
    private Long id;
    private String name;
    private String parentName;
    private String parentPhone;
    private StudentStatus status;
    private LocalDateTime createdAt;
}
