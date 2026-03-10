package com.edu.management.service;

import com.edu.management.dto.BackupConfigDto;
import com.edu.management.dto.BackupRecordDto;

import java.util.List;

public interface BackupService {

    BackupRecordDto createBackup(String description);

    List<BackupRecordDto> getAllBackups();

    void deleteBackup(Long id);

    BackupConfigDto getConfig();

    BackupConfigDto updateConfig(BackupConfigDto dto);

    void restoreBackup(Long id);

    void cleanupOldBackups();

    void importBackup(java.io.InputStream inputStream, String fileName);
}
