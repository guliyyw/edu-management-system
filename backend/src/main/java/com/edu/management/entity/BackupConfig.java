package com.edu.management.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "backup_config")
@Data
public class BackupConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer maxBackups = 30;

    @Column(nullable = false)
    private String backupInterval = "DAILY";

    @Column(nullable = false)
    private String backupTime = "02:00";

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private String backupPath = "./backups";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
