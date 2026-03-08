package com.edu.management.dto;

import com.edu.management.enums.ClassStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class ClassDto {
    
    private Long id;
    private CourseDto course;
    private TeacherDto teacher;
    private CampusDto campus;
    private String classroom;
    private Integer dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private ClassStatus status;
    private List<ClassStudentDto> students;
    private LocalDateTime createdAt;
}
