package com.edu.management.dto;

import com.edu.management.enums.ClassStatus;
import com.edu.management.enums.GradeLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
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
    private String className;
    private GradeLevel gradeLevel;
    private BigDecimal unitPrice;
    private BigDecimal teacherFee;
    private Integer defaultDayOfWeek;
    private LocalTime defaultStartTime;
    private LocalTime defaultEndTime;
    private ClassStatus status;
    private List<ClassStudentDto> students;
    private LocalDateTime createdAt;
}
