package com.edu.management.service;

import java.time.LocalDate;
import java.util.Map;

public interface ReportService {
    
    // 学生课时消耗报表
    Map<String, Object> getStudentHourReport(Long studentId, LocalDate startDate, LocalDate endDate);
    
    // 老师授课统计报表
    Map<String, Object> getTeacherLessonReport(Long teacherId, LocalDate startDate, LocalDate endDate);
    
    // 校区课时统计报表
    Map<String, Object> getCampusLessonReport(Long campusId, LocalDate startDate, LocalDate endDate);
    
    // 财务流水报表
    Map<String, Object> getFinanceReport(LocalDate startDate, LocalDate endDate, Long campusId);
    
    // 试课转化统计
    Map<String, Object> getTrialConversionReport(LocalDate startDate, LocalDate endDate);
    
    // 导出Excel
    byte[] exportStudentHoursToExcel(Long studentId, LocalDate startDate, LocalDate endDate);
    
    byte[] exportTeacherLessonsToExcel(Long teacherId, LocalDate startDate, LocalDate endDate);
    
    byte[] exportFinanceToExcel(LocalDate startDate, LocalDate endDate, Long campusId);
}
