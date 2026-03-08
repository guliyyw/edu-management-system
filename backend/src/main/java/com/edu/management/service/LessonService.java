package com.edu.management.service;

import com.edu.management.dto.LessonDto;
import com.edu.management.enums.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;

public interface LessonService {
    
    LessonDto create(LessonDto dto);
    
    LessonDto update(Long id, LessonDto dto);
    
    void delete(Long id);
    
    LessonDto getById(Long id);
    
    List<LessonDto> getByClassId(Long classId);
    
    List<LessonDto> getByTeacherIdAndDate(Long teacherId, LocalDate date);
    
    List<LessonDto> getByTeacherIdAndDateRange(Long teacherId, LocalDate startDate, LocalDate endDate);
    
    List<LessonDto> getByCampusIdAndDate(Long campusId, LocalDate date);
    
    // 获取所有课节
    List<LessonDto> getAllLessons();
    
    // 批量生成课节
    List<LessonDto> generateLessons(Long classId, LocalDate startDate, LocalDate endDate);
    
    // 签到
    void markAttendance(Long lessonId, Long studentId, AttendanceStatus status, String remark);
    
    // 修改签到记录（教务权限）
    void modifyAttendance(Long lessonId, Long studentId, AttendanceStatus status, String remark, String modifyReason, Long modifiedBy);
    
    // 检查跨校区冲突
    boolean checkCrossCampusConflict(Long teacherId, Long campusId, LocalDate date, 
                                      java.time.LocalTime startTime, java.time.LocalTime endTime, Long excludeLessonId);
}
