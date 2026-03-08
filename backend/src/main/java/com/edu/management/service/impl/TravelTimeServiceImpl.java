package com.edu.management.service.impl;

import com.edu.management.dto.CampusDto;
import com.edu.management.dto.TeacherDto;
import com.edu.management.dto.TravelTimeDto;
import com.edu.management.entity.Campus;
import com.edu.management.entity.Teacher;
import com.edu.management.entity.TravelTime;
import com.edu.management.repository.CampusRepository;
import com.edu.management.repository.TeacherRepository;
import com.edu.management.repository.TravelTimeRepository;
import com.edu.management.service.TravelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TravelTimeServiceImpl implements TravelTimeService {
    
    private final TravelTimeRepository travelTimeRepository;
    private final TeacherRepository teacherRepository;
    private final CampusRepository campusRepository;
    
    @Override
    @Transactional
    public TravelTimeDto create(TravelTimeDto dto) {
        Teacher teacher = teacherRepository.findById(dto.getTeacher().getId())
                .orElseThrow(() -> new RuntimeException("老师不存在"));
        
        Campus fromCampus = campusRepository.findById(dto.getFromCampus().getId())
                .orElseThrow(() -> new RuntimeException("起点校区不存在"));
        
        Campus toCampus = campusRepository.findById(dto.getToCampus().getId())
                .orElseThrow(() -> new RuntimeException("终点校区不存在"));
        
        TravelTime travelTime = new TravelTime();
        travelTime.setTeacher(teacher);
        travelTime.setFromCampus(fromCampus);
        travelTime.setToCampus(toCampus);
        travelTime.setTravelMinutes(dto.getTravelMinutes());
        travelTime.setEffectiveDate(dto.getEffectiveDate());
        
        travelTime = travelTimeRepository.save(travelTime);
        return toDto(travelTime);
    }
    
    @Override
    @Transactional
    public TravelTimeDto update(Long id, TravelTimeDto dto) {
        TravelTime travelTime = travelTimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("路程配置不存在"));
        
        travelTime.setTravelMinutes(dto.getTravelMinutes());
        travelTime.setEffectiveDate(dto.getEffectiveDate());
        
        travelTime = travelTimeRepository.save(travelTime);
        return toDto(travelTime);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        travelTimeRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public TravelTimeDto getById(Long id) {
        TravelTime travelTime = travelTimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("路程配置不存在"));
        return toDto(travelTime);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TravelTimeDto> getByTeacherId(Long teacherId) {
        return travelTimeRepository.findByTeacherId(teacherId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public TravelTimeDto getEffectiveTravelTime(Long teacherId, Long fromCampusId, Long toCampusId, LocalDate date) {
        return travelTimeRepository.findEffectiveTravelTime(teacherId, fromCampusId, toCampusId, date)
                .map(this::toDto)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TravelTimeDto> getByTeacherIdAndDate(Long teacherId, LocalDate date) {
        return travelTimeRepository.findByTeacherIdAndEffectiveDate(teacherId, date).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private TravelTimeDto toDto(TravelTime travelTime) {
        return TravelTimeDto.builder()
                .id(travelTime.getId())
                .teacher(TeacherDto.builder()
                        .id(travelTime.getTeacher().getId())
                        .name(travelTime.getTeacher().getName())
                        .build())
                .fromCampus(CampusDto.builder()
                        .id(travelTime.getFromCampus().getId())
                        .name(travelTime.getFromCampus().getName())
                        .build())
                .toCampus(CampusDto.builder()
                        .id(travelTime.getToCampus().getId())
                        .name(travelTime.getToCampus().getName())
                        .build())
                .travelMinutes(travelTime.getTravelMinutes())
                .effectiveDate(travelTime.getEffectiveDate())
                .createdAt(travelTime.getCreatedAt())
                .build();
    }
}
