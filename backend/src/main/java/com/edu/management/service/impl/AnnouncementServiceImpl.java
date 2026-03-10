package com.edu.management.service.impl;

import com.edu.management.dto.AnnouncementDto;
import com.edu.management.entity.Announcement;
import com.edu.management.entity.User;
import com.edu.management.repository.AnnouncementRepository;
import com.edu.management.repository.UserRepository;
import com.edu.management.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
    
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public AnnouncementDto create(AnnouncementDto dto, Long createdBy) {
        User user = userRepository.findById(createdBy)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        announcement.setIsPinned(dto.getIsPinned() != null ? dto.getIsPinned() : false);
        announcement.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        announcement.setPublishTime(dto.getPublishTime());
        announcement.setExpireTime(dto.getExpireTime());
        announcement.setCreatedBy(user);
        
        announcement = announcementRepository.save(announcement);
        return toDto(announcement);
    }
    
    @Override
    @Transactional
    public AnnouncementDto update(Long id, AnnouncementDto dto) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        announcement.setIsPinned(dto.getIsPinned());
        announcement.setIsActive(dto.getIsActive());
        announcement.setPublishTime(dto.getPublishTime());
        announcement.setExpireTime(dto.getExpireTime());
        
        announcement = announcementRepository.save(announcement);
        return toDto(announcement);
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        announcementRepository.delete(announcement);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AnnouncementDto getById(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("公告不存在"));
        return toDto(announcement);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementDto> getAll() {
        return announcementRepository.findAllByOrderByIsPinnedDescCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementDto> getActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementDto> getTop5ActiveAnnouncements() {
        return announcementRepository.findActiveAnnouncements(LocalDateTime.now()).stream()
                .limit(5)
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    private AnnouncementDto toDto(Announcement announcement) {
        return AnnouncementDto.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .type(announcement.getType())
                .isPinned(announcement.getIsPinned())
                .isActive(announcement.getIsActive())
                .publishTime(announcement.getPublishTime())
                .expireTime(announcement.getExpireTime())
                .createdBy(announcement.getCreatedBy() != null ? announcement.getCreatedBy().getId() : null)
                .createdByName(announcement.getCreatedBy() != null ? announcement.getCreatedBy().getRealName() : null)
                .createdAt(announcement.getCreatedAt())
                .updatedAt(announcement.getUpdatedAt())
                .build();
    }
}
