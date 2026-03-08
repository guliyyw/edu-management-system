package com.edu.management.service;

import com.edu.management.dto.CampusDto;

import java.util.List;

public interface CampusService {
    
    CampusDto create(CampusDto dto);
    
    CampusDto update(Long id, CampusDto dto);
    
    void delete(Long id);
    
    CampusDto getById(Long id);
    
    List<CampusDto> getAll();
    
    List<CampusDto> getActiveCampuses();
}
