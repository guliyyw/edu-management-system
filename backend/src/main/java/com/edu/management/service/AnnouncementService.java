package com.edu.management.service;

import com.edu.management.dto.AnnouncementDto;

import java.util.List;

public interface AnnouncementService {
    
    AnnouncementDto create(AnnouncementDto dto, Long createdBy);
    
    AnnouncementDto update(Long id, AnnouncementDto dto);
    
    void delete(Long id);
    
    AnnouncementDto getById(Long id);
    
    List<AnnouncementDto> getAll();
    
    List<AnnouncementDto> getActiveAnnouncements();
    
    List<AnnouncementDto> getTop5ActiveAnnouncements();
}
