package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.BackupConfigDto;
import com.edu.management.dto.BackupRecordDto;
import com.edu.management.service.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BackupController {

    private final BackupService backupService;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BackupRecordDto> createBackup(@RequestParam(required = false) String description) {
        String desc = description != null ? description : "手动备份";
        return ApiResponse.success("备份创建成功", backupService.createBackup(desc));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BackupRecordDto>> getAllBackups() {
        return ApiResponse.success(backupService.getAllBackups());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBackup(@PathVariable Long id) {
        backupService.deleteBackup(id);
        return ApiResponse.success("备份已删除", null);
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BackupConfigDto> getConfig() {
        return ApiResponse.success(backupService.getConfig());
    }

    @PutMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BackupConfigDto> updateConfig(@RequestBody BackupConfigDto dto) {
        return ApiResponse.success("配置更新成功", backupService.updateConfig(dto));
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restoreBackup(@PathVariable Long id) {
        backupService.restoreBackup(id);
        return ApiResponse.success("数据库恢复成功，请重新启动系统", null);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadBackup(@PathVariable Long id) {
        BackupRecordDto backup = backupService.getAllBackups().stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("备份不存在"));

        File file = new File(backup.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("备份文件不存在");
        }

        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + backup.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> importBackup(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("请选择要导入的备份文件");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".zip") && !fileName.endsWith(".sql"))) {
            throw new RuntimeException("不支持的文件格式，仅支持 .zip 和 .sql 文件");
        }

        try {
            backupService.importBackup(file.getInputStream(), fileName);
            return ApiResponse.success("备份导入成功，请重新启动系统", null);
        } catch (IOException e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage());
        }
    }

    @GetMapping("/database-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, String>> getDatabaseInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("url", datasourceUrl);
        info.put("username", dbUsername);
        
        // 判断数据库类型
        String dbType;
        if (datasourceUrl.contains("postgresql") || datasourceUrl.contains("postgres")) {
            dbType = "PostgreSQL";
        } else if (datasourceUrl.contains("h2")) {
            dbType = "H2";
        } else if (datasourceUrl.contains("mysql")) {
            dbType = "MySQL";
        } else {
            dbType = "Unknown";
        }
        info.put("type", dbType);
        
        // 提取数据库名称
        String dbName = extractDatabaseName(datasourceUrl);
        info.put("databaseName", dbName);
        
        return ApiResponse.success(info);
    }

    private String extractDatabaseName(String url) {
        if (url.contains("postgresql") || url.contains("mysql")) {
            int lastSlash = url.lastIndexOf('/');
            int questionMark = url.indexOf('?', lastSlash);
            if (questionMark > 0) {
                return url.substring(lastSlash + 1, questionMark);
            }
            return url.substring(lastSlash + 1);
        } else if (url.contains("h2")) {
            int fileIndex = url.indexOf("file:");
            if (fileIndex > 0) {
                String path = url.substring(fileIndex + 5);
                int semicolonIndex = path.indexOf(";");
                if (semicolonIndex > 0) {
                    path = path.substring(0, semicolonIndex);
                }
                return new File(path).getName();
            }
        }
        return "unknown";
    }
}
