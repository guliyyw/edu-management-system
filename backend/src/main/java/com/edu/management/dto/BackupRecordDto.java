package com.edu.management.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BackupRecordDto {
    private Long id;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileSizeFormatted;
    private String backupType;
    private String description;
    private String status;
    private String errorMessage;
    private String databaseType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
