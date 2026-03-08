package com.edu.management.service;

import com.edu.management.dto.CourseDto;

import java.util.List;

public interface CourseService {
    
    CourseDto create(CourseDto dto);
    
    CourseDto update(Long id, CourseDto dto);
    
    void delete(Long id);
    
    CourseDto getById(Long id);
    
    List<CourseDto> getAll();
    
    List<CourseDto> getActiveCourses();
}
