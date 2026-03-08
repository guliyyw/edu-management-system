package com.edu.management.dto;

import com.edu.management.enums.CampusStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CampusDto {
    
    private Long id;
    private String name;
    private String address;
    private CampusStatus status;
    private LocalDateTime createdAt;
}
