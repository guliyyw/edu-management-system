package com.edu.management.service;

import com.edu.management.dto.ClassDto;

import java.util.List;

public interface ClassService {
    
    ClassDto create(ClassDto dto);
    
    ClassDto update(Long id, ClassDto dto);
    
    void delete(Long id);
    
    ClassDto getById(Long id);
    
    List<ClassDto> getAll();
    
    List<ClassDto> getByTeacherId(Long teacherId);
    
    List<ClassDto> getByCampusId(Long campusId);
    
    List<ClassDto> getByStudentId(Long studentId);
    
    // 检查排课冲突
    boolean checkScheduleConflict(Long teacherId, Long campusId, Integer dayOfWeek, 
                                   java.time.LocalTime startTime, java.time.LocalTime endTime, Long excludeClassId);
    
    // 添加学生到班级
    void addStudentToClass(Long classId, Long studentId, Boolean isTrial);
    
    // 从班级移除学生
    void removeStudentFromClass(Long classId, Long studentId);
    
    // 试课转正
    void convertTrialStudent(Long classId, Long studentId);
}
