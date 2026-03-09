package com.edu.management.service.impl;

import com.edu.management.dto.CampusDto;
import com.edu.management.dto.TeacherDto;
import com.edu.management.dto.TeacherRequest;
import com.edu.management.entity.Campus;
import com.edu.management.entity.Teacher;
import com.edu.management.enums.TeacherStatus;
import com.edu.management.repository.CampusRepository;
import com.edu.management.repository.TeacherRepository;
import com.edu.management.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final CampusRepository campusRepository;

    @Override
    @Transactional
    public TeacherDto create(TeacherRequest request) {
        Teacher teacher = new Teacher();
        teacher.setName(request.getName());
        teacher.setPhone(request.getPhone());
        teacher.setStatus(TeacherStatus.ACTIVE);

        // 设置所属校区
        if (request.getCampusIds() != null && !request.getCampusIds().isEmpty()) {
            Set<Campus> campuses = new HashSet<>();
            for (Long campusId : request.getCampusIds()) {
                Campus campus = campusRepository.findById(campusId)
                        .orElseThrow(() -> new RuntimeException("校区不存在"));
                campuses.add(campus);
            }
            teacher.setCampuses(campuses);
        }

        teacher = teacherRepository.save(teacher);
        return toDto(teacher);
    }

    @Override
    @Transactional
    public TeacherDto update(Long id, TeacherRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("老师不存在"));

        teacher.setName(request.getName());
        teacher.setPhone(request.getPhone());

        // 更新所属校区
        if (request.getCampusIds() != null) {
            Set<Campus> campuses = new HashSet<>();
            for (Long campusId : request.getCampusIds()) {
                Campus campus = campusRepository.findById(campusId)
                        .orElseThrow(() -> new RuntimeException("校区不存在"));
                campuses.add(campus);
            }
            teacher.setCampuses(campuses);
        }

        teacher = teacherRepository.save(teacher);
        return toDto(teacher);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("老师不存在"));

        // 软删除逻辑：第一次标记为DELETED，第二次彻底删除
        if (teacher.getStatus() == TeacherStatus.DELETED) {
            teacherRepository.delete(teacher);
        } else {
            teacher.setStatus(TeacherStatus.DELETED);
            teacherRepository.save(teacher);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TeacherDto getById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("老师不存在"));
        return toDto(teacher);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TeacherDto> getAll() {
        return teacherRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TeacherDto> getActiveTeachers() {
        return teacherRepository.findByStatus(TeacherStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TeacherDto> getByCampusId(Long campusId) {
        return teacherRepository.findByCampusIdAndStatus(campusId, TeacherStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private TeacherDto toDto(Teacher teacher) {
        Set<CampusDto> campusDtos = teacher.getCampuses().stream()
                .map(c -> CampusDto.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .address(c.getAddress())
                        .status(c.getStatus())
                        .build())
                .collect(Collectors.toSet());
        
        return TeacherDto.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .phone(teacher.getPhone())
                .campuses(campusDtos)
                .status(teacher.getStatus())
                .createdAt(teacher.getCreatedAt())
                .build();
    }
}
