package com.edu.management.config;

import com.edu.management.entity.BackupConfig;
import com.edu.management.repository.BackupConfigRepository;
import com.edu.management.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BackupScheduler implements SchedulingConfigurer {

    private final BackupService backupService;
    private final BackupConfigRepository backupConfigRepository;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());

        // 动态配置定时任务
        taskRegistrar.addTriggerTask(
            () -> {
                try {
                    log.info("开始执行定时备份任务...");
                    backupService.createBackup("自动备份 - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    log.info("定时备份任务完成");
                } catch (Exception e) {
                    log.error("定时备份任务失败", e);
                }
            },
            triggerContext -> {
                Optional<BackupConfig> configOpt = backupConfigRepository.findFirstByOrderByIdAsc();
                if (configOpt.isPresent()) {
                    BackupConfig config = configOpt.get();
                    if (!config.getEnabled()) {
                        return null; // 禁用时不触发
                    }

                    // 根据备份间隔和时间生成 cron 表达式
                    String cron = generateCronExpression(config);
                    Date nextExecution = new CronTrigger(cron).nextExecutionTime(triggerContext);
                    return nextExecution != null ? nextExecution.toInstant() : null;
                }
                // 默认每天凌晨2点
                Date nextExecution = new CronTrigger("0 0 2 * * ?").nextExecutionTime(triggerContext);
                return nextExecution != null ? nextExecution.toInstant() : null;
            }
        );
    }

    private String generateCronExpression(BackupConfig config) {
        String interval = config.getBackupInterval();
        String time = config.getBackupTime(); // 格式: HH:mm

        String[] timeParts = time.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        switch (interval) {
            case "HOURLY":
                return "0 0 * * * ?"; // 每小时
            case "DAILY":
                return String.format("0 %d %d * * ?", minute, hour); // 每天
            case "WEEKLY":
                return String.format("0 %d %d ? * SUN", minute, hour); // 每周日
            case "MONTHLY":
                return String.format("0 %d %d 1 * ?", minute, hour); // 每月1号
            default:
                return "0 0 2 * * ?"; // 默认每天凌晨2点
        }
    }

    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(1);
    }
}
