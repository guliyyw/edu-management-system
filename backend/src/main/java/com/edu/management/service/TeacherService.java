package com.edu.management.service;

import com.edu.management.dto.TeacherDto;

import java.util.List;

public interface TeacherService {
    
    TeacherDto create(TeacherDto dto);
    
    TeacherDto update(Long id, TeacherDto dto);
    
    void delete(Long id);
    
    TeacherDto getById(Long id);
    
    List<TeacherDto> getAll();
    
    List<TeacherDto> getActiveTeachers();
    
    List<TeacherDto> getByCampusId(Long campusId);
}
