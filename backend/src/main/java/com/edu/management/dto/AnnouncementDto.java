package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementDto {
    
    private Long id;
    private String title;
    private String content;
    private String type;
    private Boolean isPinned;
    private Boolean isActive;
    private LocalDateTime publishTime;
    private LocalDateTime expireTime;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
