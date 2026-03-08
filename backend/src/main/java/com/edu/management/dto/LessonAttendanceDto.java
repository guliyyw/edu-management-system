package com.edu.management.dto;

import com.edu.management.enums.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LessonAttendanceDto {
    
    private Long id;
    private StudentDto student;
    private AttendanceStatus status;
    private String remark;
    private Boolean isTrial;
    private Long modifiedBy;
    private LocalDateTime modifiedAt;
    private String modifyReason;
    private LocalDateTime createdAt;
}
