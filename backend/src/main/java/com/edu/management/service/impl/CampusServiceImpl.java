package com.edu.management.service.impl;

import com.edu.management.dto.CampusDto;
import com.edu.management.entity.Campus;
import com.edu.management.enums.CampusStatus;
import com.edu.management.repository.CampusRepository;
import com.edu.management.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampusServiceImpl implements CampusService {
    
    private final CampusRepository campusRepository;
    
    @Override
    @Transactional
    public CampusDto create(CampusDto dto) {
        Campus campus = new Campus();
        campus.setName(dto.getName());
        campus.setAddress(dto.getAddress());
        campus.setStatus(CampusStatus.ACTIVE);
        
        campus = campusRepository.save(campus);
        return toDto(campus);
    }
    
    @Override
    @Transactional
    public CampusDto update(Long id, CampusDto dto) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("校区不存在"));
        
        campus.setName(dto.getName());
        campus.setAddress(dto.getAddress());
        if (dto.getStatus() != null) {
            campus.setStatus(dto.getStatus());
        }
        
        campus = campusRepository.save(campus);
        return toDto(campus);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("校区不存在"));

        // 软删除逻辑：第一次标记为DELETED，第二次彻底删除
        if (campus.getStatus() == CampusStatus.DELETED) {
            campusRepository.delete(campus);
        } else {
            campus.setStatus(CampusStatus.DELETED);
            campusRepository.save(campus);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CampusDto getById(Long id) {
        Campus campus = campusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("校区不存在"));
        return toDto(campus);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CampusDto> getAll() {
        return campusRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CampusDto> getActiveCampuses() {
        return campusRepository.findByStatus(CampusStatus.ACTIVE).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private CampusDto toDto(Campus campus) {
        return CampusDto.builder()
                .id(campus.getId())
                .name(campus.getName())
                .address(campus.getAddress())
                .status(campus.getStatus())
                .createdAt(campus.getCreatedAt())
                .build();
    }
}
