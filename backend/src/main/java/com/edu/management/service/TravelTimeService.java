package com.edu.management.service;

import com.edu.management.dto.TravelTimeDto;

import java.time.LocalDate;
import java.util.List;

public interface TravelTimeService {
    
    TravelTimeDto create(TravelTimeDto dto);
    
    TravelTimeDto update(Long id, TravelTimeDto dto);
    
    void delete(Long id);
    
    TravelTimeDto getById(Long id);
    
    List<TravelTimeDto> getByTeacherId(Long teacherId);
    
    // 获取有效的路程时间
    TravelTimeDto getEffectiveTravelTime(Long teacherId, Long fromCampusId, Long toCampusId, LocalDate date);
    
    // 获取老师在指定日期的所有路程配置
    List<TravelTimeDto> getByTeacherIdAndDate(Long teacherId, LocalDate date);
}
