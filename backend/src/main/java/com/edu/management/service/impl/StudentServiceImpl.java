package com.edu.management.service.impl;

import com.edu.management.dto.ClassDto;
import com.edu.management.dto.StudentDto;
import com.edu.management.entity.Student;
import com.edu.management.enums.ClassStatus;
import com.edu.management.enums.StudentStatus;
import com.edu.management.repository.ClassRepository;
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
    private final ClassRepository classRepository;
    private final ClassServiceImpl classServiceImpl;
    
    @Override
    @Transactional
    public StudentDto create(StudentDto dto) {
        Student student = new Student();
        student.setName(dto.getName());
        student.setParentName(dto.getParentName());
        student.setParentPhone(dto.getParentPhone());
        student.setGradeLevel(dto.getGradeLevel());
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
        if (dto.getGradeLevel() != null) {
            student.setGradeLevel(dto.getGradeLevel());
        }
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

        // 软删除逻辑：第一次标记为DELETED，第二次彻底删除
        if (student.getStatus() == StudentStatus.DELETED) {
            studentRepository.delete(student);
        } else {
            student.setStatus(StudentStatus.DELETED);
            studentRepository.save(student);
        }
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
    
    @Override
    @Transactional(readOnly = true)
    public List<ClassDto> getStudentClasses(Long studentId) {
        return classRepository.findByStudentIdAndStatus(studentId, ClassStatus.ACTIVE).stream()
                .map(classServiceImpl::toDto)
                .collect(Collectors.toList());
    }

    private StudentDto toDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .name(student.getName())
                .parentName(student.getParentName())
                .parentPhone(student.getParentPhone())
                .gradeLevel(student.getGradeLevel())
                .status(student.getStatus())
                .createdAt(student.getCreatedAt())
                .build();
    }
}
