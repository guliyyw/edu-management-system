package com.edu.management.dto;

import com.edu.management.enums.CourseStatus;
import com.edu.management.enums.CourseType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CourseDto {
    
    private Long id;
    private String name;
    private CourseType type;
    private BigDecimal unitPrice;
    private BigDecimal trialPrice;
    private CourseStatus status;
    private LocalDateTime createdAt;
}
