package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupConfigDto {
    private Long id;
    private Integer maxBackups;
    private String backupInterval;
    private String backupTime;
    private Boolean enabled;
    private String backupPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
