package com.edu.management.service.impl;

import com.edu.management.dto.BackupConfigDto;
import com.edu.management.dto.BackupRecordDto;
import com.edu.management.entity.BackupConfig;
import com.edu.management.entity.BackupRecord;
import com.edu.management.repository.BackupConfigRepository;
import com.edu.management.repository.BackupRecordRepository;
import com.edu.management.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupServiceImpl implements BackupService {

    private final BackupRecordRepository backupRecordRepository;
    private final BackupConfigRepository backupConfigRepository;
    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String BACKUP_TYPE_MANUAL = "MANUAL";
    private static final String BACKUP_TYPE_AUTO = "AUTO";

    /**
     * 获取数据库类型
     */
    private DatabaseType getDatabaseType() {
        if (datasourceUrl.contains("postgresql") || datasourceUrl.contains("postgres")) {
            return DatabaseType.POSTGRESQL;
        } else if (datasourceUrl.contains("h2")) {
            return DatabaseType.H2;
        } else if (datasourceUrl.contains("mysql")) {
            return DatabaseType.MYSQL;
        }
        return DatabaseType.UNKNOWN;
    }

    enum DatabaseType {
        POSTGRESQL, H2, MYSQL, UNKNOWN
    }

    @Override
    @Transactional
    public BackupRecordDto createBackup(String description) {
        log.info("开始创建备份，描述: {}", description);
        BackupConfig config = getOrCreateConfig();
        log.info("备份配置: path={}, maxBackups={}", config.getBackupPath(), config.getMaxBackups());

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_" + timestamp + ".sql";
        String zipFileName = "backup_" + timestamp + ".zip";

        // 确保备份目录存在，使用绝对路径
        String backupPathStr = config.getBackupPath();
        Path backupPath = Paths.get(backupPathStr);
        log.info("备份路径: {}", backupPath.toAbsolutePath());

        try {
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
                log.info("创建备份目录成功: {}", backupPath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("创建备份目录失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建备份目录失败: " + e.getMessage());
        }

        String sqlFilePath = backupPath.resolve(fileName).toString();
        String zipFilePath = backupPath.resolve(zipFileName).toString();
        log.info("SQL文件路径: {}, ZIP文件路径: {}", sqlFilePath, zipFilePath);

        BackupRecord record = new BackupRecord();
        record.setFileName(zipFileName);
        record.setFilePath(zipFilePath);
        record.setBackupType(description != null && description.contains("手动") ? BACKUP_TYPE_MANUAL : BACKUP_TYPE_AUTO);
        record.setDescription(description);
        record.setStatus("PENDING");
        record.setFileSize(0L);
        record.setDatabaseType(getDatabaseType().name());

        record = backupRecordRepository.save(record);
        log.info("备份记录已创建，ID: {}", record.getId());

        try {
            // 根据数据库类型执行备份
            DatabaseType dbType = getDatabaseType();
            log.info("数据库类型: {}", dbType);

            switch (dbType) {
                case POSTGRESQL:
                    log.info("开始导出 PostgreSQL 数据库...");
                    exportPostgreSQLDatabase(sqlFilePath);
                    break;
                case H2:
                    log.info("开始导出 H2 数据库...");
                    exportH2Database(sqlFilePath);
                    break;
                case MYSQL:
                    log.info("开始导出 MySQL 数据库...");
                    exportMySQLDatabase(sqlFilePath);
                    break;
                default:
                    throw new RuntimeException("不支持的数据库类型: " + datasourceUrl);
            }
            log.info("数据库导出完成");

            // 检查SQL文件是否生成
            File sqlFile = new File(sqlFilePath);
            if (!sqlFile.exists()) {
                throw new RuntimeException("SQL备份文件未生成: " + sqlFilePath);
            }
            log.info("SQL文件已生成，大小: {} bytes", sqlFile.length());

            // 压缩SQL文件
            log.info("开始压缩SQL文件...");
            zipFile(sqlFilePath, zipFilePath);
            log.info("压缩完成");

            // 删除原始SQL文件
            Files.deleteIfExists(Paths.get(sqlFilePath));

            // 更新记录
            File zipFile = new File(zipFilePath);
            record.setFileSize(zipFile.length());
            record.setStatus(STATUS_SUCCESS);
            record = backupRecordRepository.save(record);
            log.info("备份成功完成，文件大小: {} bytes", zipFile.length());

            // 清理旧备份
            cleanupOldBackups();

            return toDto(record);
        } catch (Exception e) {
            log.error("备份失败: {}", e.getMessage(), e);
            record.setStatus(STATUS_FAILED);
            record.setErrorMessage(e.getMessage());
            backupRecordRepository.save(record);
            throw new RuntimeException("备份失败: " + e.getMessage());
        }
    }

    /**
     * 导出 PostgreSQL 数据库
     */
    private void exportPostgreSQLDatabase(String outputFile) throws Exception {
        // 解析数据库连接信息
        String dbName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        int port = extractPort(datasourceUrl);

        log.info("PostgreSQL 连接信息: host={}, port={}, database={}, user={}", host, port, dbName, dbUsername);

        // 构建 pg_dump 命令
        ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-p", String.valueOf(port),
                "-U", dbUsername,
                "-d", dbName,
                "-f", outputFile,
                "--verbose"
        );

        // 设置环境变量 PGPASSWORD
        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // 读取输出
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("pg_dump: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("pg_dump 执行失败，退出码: " + exitCode + ", 输出: " + output);
        }

        log.info("PostgreSQL 数据库已导出到: {}", outputFile);
    }

    /**
     * 导出 H2 数据库
     */
    private void exportH2Database(String outputFile) throws SQLException, IOException {
        // 确保目录存在
        File outputDir = new File(outputFile).getParentFile();
        if (outputDir != null && !outputDir.exists()) {
            outputDir.mkdirs();
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // H2 SCRIPT TO 命令导出数据库
            String scriptSql = "SCRIPT TO '" + outputFile.replace("\\", "/") + "'";
            stmt.execute(scriptSql);

            log.info("H2数据库已导出到: {}", outputFile);
        }
    }

    /**
     * 导出 MySQL 数据库
     */
    private void exportMySQLDatabase(String outputFile) throws Exception {
        String dbName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        int port = extractPort(datasourceUrl);

        log.info("MySQL 连接信息: host={}, port={}, database={}, user={}", host, port, dbName, dbUsername);

        ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "-h", host,
                "-P", String.valueOf(port),
                "-u", dbUsername,
                "-p" + dbPassword,
                "--databases", dbName,
                "--result-file", outputFile,
                "--verbose"
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysqldump 执行失败，退出码: " + exitCode + ", 输出: " + output);
        }

        log.info("MySQL 数据库已导出到: {}", outputFile);
    }

    /**
     * 从 JDBC URL 提取数据库名
     */
    private String extractDatabaseName(String url) {
        // PostgreSQL: jdbc:postgresql://host:port/dbname
        // H2: jdbc:h2:file:~/path/to/db
        // MySQL: jdbc:mysql://host:port/dbname

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

    /**
     * 从 JDBC URL 提取主机
     */
    private String extractHost(String url) {
        if (url.contains("postgresql") || url.contains("mysql")) {
            int start = url.indexOf("://") + 3;
            int end = url.lastIndexOf(':');
            if (end < start) {
                end = url.indexOf('/', start);
            }
            if (end > start) {
                return url.substring(start, end);
            }
        }
        return "localhost";
    }

    /**
     * 从 JDBC URL 提取端口
     */
    private int extractPort(String url) {
        if (url.contains("postgresql")) {
            int start = url.lastIndexOf(':') + 1;
            int end = url.indexOf('/', start);
            if (end > start) {
                try {
                    return Integer.parseInt(url.substring(start, end));
                } catch (NumberFormatException e) {
                    return 5432;
                }
            }
            return 5432;
        } else if (url.contains("mysql")) {
            int start = url.lastIndexOf(':') + 1;
            int end = url.indexOf('/', start);
            if (end > start) {
                try {
                    return Integer.parseInt(url.substring(start, end));
                } catch (NumberFormatException e) {
                    return 3306;
                }
            }
            return 3306;
        }
        return 5432;
    }

    private void zipFile(String sourceFile, String zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(sourceFile)) {

            ZipEntry zipEntry = new ZipEntry(new File(sourceFile).getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }

            zos.closeEntry();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BackupRecordDto> getAllBackups() {
        return backupRecordRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBackup(Long id) {
        BackupRecord record = backupRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("备份记录不存在"));

        // 删除文件
        try {
            Files.deleteIfExists(Paths.get(record.getFilePath()));
        } catch (IOException e) {
            log.error("删除备份文件失败", e);
        }

        backupRecordRepository.delete(record);
    }

    @Override
    @Transactional(readOnly = true)
    public BackupConfigDto getConfig() {
        BackupConfig config = getOrCreateConfig();
        return toDto(config);
    }

    @Override
    @Transactional
    public BackupConfigDto updateConfig(BackupConfigDto dto) {
        BackupConfig config = getOrCreateConfig();

        if (dto.getMaxBackups() != null) {
            config.setMaxBackups(dto.getMaxBackups());
        }
        if (dto.getBackupInterval() != null) {
            config.setBackupInterval(dto.getBackupInterval());
        }
        if (dto.getBackupTime() != null) {
            config.setBackupTime(dto.getBackupTime());
        }
        if (dto.getEnabled() != null) {
            config.setEnabled(dto.getEnabled());
        }
        if (dto.getBackupPath() != null) {
            String backupPath = validateAndNormalizeBackupPath(dto.getBackupPath());
            config.setBackupPath(backupPath);
        }

        config = backupConfigRepository.save(config);
        return toDto(config);
    }

    /**
     * 验证并规范化备份路径
     */
    private String validateAndNormalizeBackupPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new RuntimeException("备份路径不能为空");
        }

        path = path.trim();

        // 规范化路径分隔符
        path = path.replace("/", File.separator).replace("\\", File.separator);

        Path normalizedPath;
        try {
            if (path.startsWith(".")) {
                // 相对路径：相对于应用根目录
                String rootPath = getApplicationRootPath();
                String relativePath = path.replace("." + File.separator, "").replace(".", "");
                normalizedPath = Paths.get(rootPath, relativePath).toAbsolutePath().normalize();
            } else if (path.startsWith(File.separator) || (path.length() > 1 && path.charAt(1) == ':')) {
                // 绝对路径（Unix/Linux 或 Windows）
                normalizedPath = Paths.get(path).toAbsolutePath().normalize();
            } else {
                // 其他情况视为相对路径
                normalizedPath = Paths.get(getApplicationRootPath(), path).toAbsolutePath().normalize();
            }
        } catch (Exception e) {
            throw new RuntimeException("备份路径格式无效: " + e.getMessage());
        }

        // 安全检查：防止路径遍历攻击
        String rootPath = getApplicationRootPath();
        Path rootPathNormalized = Paths.get(rootPath).toAbsolutePath().normalize();

        // 允许的路径前缀（可以根据需要扩展）
        boolean isAllowed = normalizedPath.startsWith(rootPathNormalized) ||
                           path.startsWith(File.separator) ||
                           (path.length() > 1 && path.charAt(1) == ':');

        if (!isAllowed) {
            throw new RuntimeException("备份路径不在允许范围内，请使用绝对路径或应用目录下的相对路径");
        }

        // 验证路径是否可写
        try {
            Path parent = normalizedPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            // 尝试创建测试文件验证写入权限
            Path testFile = normalizedPath.resolve(".write_test");
            Files.createFile(testFile);
            Files.deleteIfExists(testFile);
        } catch (Exception e) {
            throw new RuntimeException("备份路径无法写入，请检查权限: " + e.getMessage());
        }

        return normalizedPath.toString();
    }

    @Override
    public void restoreBackup(Long id) {
        BackupRecord record = backupRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("备份记录不存在"));

        if (!STATUS_SUCCESS.equals(record.getStatus())) {
            throw new RuntimeException("只能恢复成功的备份");
        }

        String sqlFilePath = null;
        try {
            // 解压备份文件
            sqlFilePath = unzipFile(record.getFilePath());

            // 根据数据库类型恢复
            DatabaseType dbType = DatabaseType.valueOf(record.getDatabaseType());
            switch (dbType) {
                case POSTGRESQL:
                    restorePostgreSQLDatabase(sqlFilePath);
                    break;
                case H2:
                    restoreH2Database(sqlFilePath);
                    break;
                case MYSQL:
                    restoreMySQLDatabase(sqlFilePath);
                    break;
                default:
                    throw new RuntimeException("不支持的数据库类型: " + record.getDatabaseType());
            }

            log.info("数据库已从备份恢复: {}", record.getFileName());
        } catch (Exception e) {
            log.error("恢复备份失败", e);
            throw new RuntimeException("恢复备份失败: " + e.getMessage());
        } finally {
            // 删除解压的临时文件
            if (sqlFilePath != null) {
                try {
                    Files.deleteIfExists(Paths.get(sqlFilePath));
                    // 删除临时目录
                    Files.deleteIfExists(Paths.get(sqlFilePath).getParent());
                } catch (IOException e) {
                    log.warn("清理临时文件失败: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 恢复 PostgreSQL 数据库
     */
    private void restorePostgreSQLDatabase(String sqlFilePath) throws Exception {
        String dbName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        int port = extractPort(datasourceUrl);

        log.info("开始恢复 PostgreSQL 数据库: host={}, port={}, database={}", host, port, dbName);

        ProcessBuilder pb = new ProcessBuilder(
                "psql",
                "-h", host,
                "-p", String.valueOf(port),
                "-U", dbUsername,
                "-d", dbName,
                "-f", sqlFilePath,
                "--verbose"
        );

        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("psql: {}", line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("psql 执行失败，退出码: " + exitCode + ", 输出: " + output);
        }

        log.info("PostgreSQL 数据库恢复完成");
    }

    /**
     * 恢复 MySQL 数据库
     */
    private void restoreMySQLDatabase(String sqlFilePath) throws Exception {
        String dbName = extractDatabaseName(datasourceUrl);
        String host = extractHost(datasourceUrl);
        int port = extractPort(datasourceUrl);

        log.info("开始恢复 MySQL 数据库: host={}, port={}, database={}", host, port, dbName);

        ProcessBuilder pb = new ProcessBuilder(
                "mysql",
                "-h", host,
                "-P", String.valueOf(port),
                "-u", dbUsername,
                "-p" + dbPassword,
                dbName,
                "-e", "source " + sqlFilePath
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysql 执行失败，退出码: " + exitCode + ", 输出: " + output);
        }

        log.info("MySQL 数据库恢复完成");
    }

    private String unzipFile(String zipFilePath) throws IOException {
        Path tempDir = Files.createTempDirectory("backup_restore");
        String sqlFileName = new File(zipFilePath).getName().replace(".zip", ".sql");
        Path outputPath = tempDir.resolve(sqlFileName);

        try (java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(zipFilePath))) {
            java.util.zip.ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            }
            zis.closeEntry();
        }

        return outputPath.toString();
    }

    private void restoreH2Database(String sqlFilePath) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 先清理现有数据库对象，避免表已存在的错误
            // 注意：不使用 DELETE FILES，只删除表结构而不删除数据库文件
            log.info("开始清理现有数据库对象...");
            stmt.execute("DROP ALL OBJECTS");
            log.info("数据库对象清理完成");

            // H2 RUNSCRIPT FROM 命令恢复数据库
            String scriptSql = "RUNSCRIPT FROM '" + sqlFilePath.replace("\\", "/") + "'";
            stmt.execute(scriptSql);

            log.info("H2数据库已从文件恢复: {}", sqlFilePath);
        }
    }

    @Override
    public void importBackup(InputStream inputStream, String fileName) {
        log.info("开始导入备份文件: {}", fileName);

        // 验证文件格式
        if (!fileName.endsWith(".zip") && !fileName.endsWith(".sql")) {
            throw new RuntimeException("不支持的文件格式，仅支持 .zip 和 .sql 文件");
        }

        Path tempDir = null;
        Path tempFilePath = null;

        try {
            // 创建临时文件
            tempDir = Files.createTempDirectory("backup_import");
            String tempFileName = fileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
            tempFilePath = tempDir.resolve(tempFileName);

            // 将上传的文件保存到临时目录
            try (InputStream is = inputStream) {
                Files.copy(is, tempFilePath);
            }
            log.info("备份文件已保存到临时目录: {}", tempFilePath);

            String sqlFilePath;

            // 如果是 zip 文件，先解压
            if (fileName.endsWith(".zip")) {
                sqlFilePath = unzipFile(tempFilePath.toString());
                // 删除临时的 zip 文件
                Files.deleteIfExists(tempFilePath);
            } else {
                sqlFilePath = tempFilePath.toString();
            }

            // 验证 SQL 文件
            File sqlFile = new File(sqlFilePath);
            if (!sqlFile.exists() || sqlFile.length() == 0) {
                throw new RuntimeException("SQL 文件无效或为空");
            }

            // 恢复数据库
            DatabaseType dbType = getDatabaseType();
            switch (dbType) {
                case POSTGRESQL:
                    restorePostgreSQLDatabase(sqlFilePath);
                    break;
                case H2:
                    restoreH2Database(sqlFilePath);
                    break;
                case MYSQL:
                    restoreMySQLDatabase(sqlFilePath);
                    break;
                default:
                    throw new RuntimeException("不支持的数据库类型: " + dbType);
            }

            log.info("备份文件导入成功: {}", fileName);
        } catch (Exception e) {
            log.error("导入备份失败: {}", e.getMessage(), e);
            throw new RuntimeException("导入备份失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            try {
                if (tempFilePath != null) {
                    Files.deleteIfExists(tempFilePath);
                }
                if (tempDir != null) {
                    Files.deleteIfExists(tempDir);
                }
            } catch (IOException e) {
                log.warn("清理临时文件失败: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void cleanupOldBackups() {
        BackupConfig config = getOrCreateConfig();
        int maxBackups = config.getMaxBackups();

        List<BackupRecord> allBackups = backupRecordRepository.findAllByOrderByCreatedAtDesc();

        if (allBackups.size() > maxBackups) {
            List<BackupRecord> toDelete = allBackups.stream()
                    .skip(maxBackups)
                    .collect(Collectors.toList());

            for (BackupRecord record : toDelete) {
                try {
                    Files.deleteIfExists(Paths.get(record.getFilePath()));
                    backupRecordRepository.delete(record);
                    log.info("已清理旧备份: {}", record.getFileName());
                } catch (IOException e) {
                    log.error("删除旧备份文件失败: {}", record.getFileName(), e);
                }
            }
        }
    }

    private BackupConfig getOrCreateConfig() {
        return backupConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    BackupConfig config = new BackupConfig();
                    config.setMaxBackups(30);
                    config.setBackupInterval("DAILY");
                    config.setBackupTime("02:00");
                    config.setEnabled(true);
                    // 使用程序所在目录作为备份路径
                    String defaultBackupPath = getApplicationRootPath() + File.separator + "backups";
                    config.setBackupPath(defaultBackupPath);
                    return backupConfigRepository.save(config);
                });
    }

    /**
     * 获取程序根目录路径
     * 优先使用类路径所在目录，确保在 jar 包运行时也能正确定位
     */
    private String getApplicationRootPath() {
        try {
            // 获取类文件所在路径
            String classPath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            File classFile = new File(java.net.URLDecoder.decode(classPath, "UTF-8"));

            // 如果运行在 jar 包中，获取 jar 包所在目录
            if (classFile.isFile()) {
                // 是 jar 文件，返回 jar 所在目录
                return classFile.getParentFile().getAbsolutePath();
            } else {
                // 是 classes 目录（开发环境），返回项目根目录
                // classes 目录通常在 target/classes 或 build/classes，需要向上两级
                File projectRoot = classFile.getParentFile().getParentFile();
                return projectRoot.getAbsolutePath();
            }
        } catch (Exception e) {
            log.warn("无法确定程序根目录，使用当前工作目录: {}", e.getMessage());
            return System.getProperty("user.dir");
        }
    }

    private BackupRecordDto toDto(BackupRecord record) {
        return BackupRecordDto.builder()
                .id(record.getId())
                .fileName(record.getFileName())
                .filePath(record.getFilePath())
                .fileSize(record.getFileSize())
                .fileSizeFormatted(formatFileSize(record.getFileSize()))
                .backupType(record.getBackupType())
                .description(record.getDescription())
                .status(record.getStatus())
                .errorMessage(record.getErrorMessage())
                .databaseType(record.getDatabaseType())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private BackupConfigDto toDto(BackupConfig config) {
        return BackupConfigDto.builder()
                .id(config.getId())
                .maxBackups(config.getMaxBackups())
                .backupInterval(config.getBackupInterval())
                .backupTime(config.getBackupTime())
                .enabled(config.getEnabled())
                .backupPath(config.getBackupPath())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
}
