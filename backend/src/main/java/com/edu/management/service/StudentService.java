package com.edu.management.service;

import com.edu.management.dto.StudentDto;

import java.util.List;

public interface StudentService {
    
    StudentDto create(StudentDto dto);
    
    StudentDto update(Long id, StudentDto dto);
    
    void delete(Long id);
    
    StudentDto getById(Long id);
    
    List<StudentDto> getAll();
    
    List<StudentDto> search(String keyword);
}
