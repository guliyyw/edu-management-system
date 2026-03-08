package com.edu.management.service.impl;

import com.edu.management.dto.StudentDto;
import com.edu.management.entity.Student;
import com.edu.management.enums.StudentStatus;
import com.edu.management.repository.StudentRepository;
import com.edu.management.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    
    private final StudentRepository studentRepository;
    
    @Override
    @Transactional
    public StudentDto create(StudentDto dto) {
        Student student = new Student();
        student.setName(dto.getName());
        student.setParentName(dto.getParentName());
        student.setParentPhone(dto.getParentPhone());
        student.setStatus(StudentStatus.ACTIVE);
        
        student = studentRepository.save(student);
        return toDto(student);
    }
    
    @Override
    @Transactional
    public StudentDto update(Long id, StudentDto dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        
        student.setName(dto.getName());
        student.setParentName(dto.getParentName());
        student.setParentPhone(dto.getParentPhone());
        if (dto.getStatus() != null) {
            student.setStatus(dto.getStatus());
        }
        
        student = studentRepository.save(student);
        return toDto(student);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        student.setStatus(StudentStatus.INACTIVE);
        studentRepository.save(student);
    }
    
    @Override
    @Transactional(readOnly = true)
    public StudentDto getById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
        return toDto(student);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> getAll() {
        return studentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentDto> search(String keyword) {
        return studentRepository.searchByKeyword(keyword).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private StudentDto toDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .name(student.getName())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .status(student.getStatus())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
