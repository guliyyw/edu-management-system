package com.edu.management.service.impl;

import com.edu.management.dto.CourseDto;
import com.edu.management.entity.Course;
import com.edu.management.enums.CourseStatus;
import com.edu.management.repository.CourseRepository;
import com.edu.management.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    
    private final CourseRepository courseRepository;
    
    @Override
    @Transactional
    public CourseDto create(CourseDto dto) {
        Course course = new Course();
        course.setName(dto.getName());
        course.setType(dto.getType());
        course.setUnitPrice(dto.getUnitPrice());
        course.setTrialPrice(dto.getTrialPrice());
        course.setStatus(CourseStatus.ACTIVE);
        
        course = courseRepository.save(course);
        return toDto(course);
    }
    
    @Override
    @Transactional
    public CourseDto update(Long id, CourseDto dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        
        course.setName(dto.getName());
        course.setType(dto.getType());
        course.setUnitPrice(dto.getUnitPrice());
        course.setTrialPrice(dto.getTrialPrice());
        if (dto.getStatus() != null) {
            course.setStatus(dto.getStatus());
        }
        
        course = courseRepository.save(course);
        return toDto(course);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        course.setStatus(CourseStatus.INACTIVE);
        courseRepository.save(course);
    }
    
    @Override
    @Transactional(readOnly = true)
    public CourseDto getById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("课程不存在"));
        return toDto(course);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAll() {
        return courseRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getActiveCourses() {
        return courseRepository.findByStatus(CourseStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private CourseDto toDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .name(course.getName())
                .type(course.getType())
                .unitPrice(course.getUnitPrice())
                .trialPrice(course.getTrialPrice())
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .build();
    }
}
