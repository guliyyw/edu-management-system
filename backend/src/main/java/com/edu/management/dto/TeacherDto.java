package com.edu.management.dto;

import com.edu.management.enums.TeacherStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class TeacherDto {
    
    private Long id;
    private String name;
    private String phone;
    private Set<CampusDto> campuses;
    private TeacherStatus status;
    private LocalDateTime createdAt;
}
