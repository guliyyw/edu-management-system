package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class DebugController {

    @PersistenceContext
    private EntityManager entityManager;

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    /**
     * 获取数据库信息
     */
    @GetMapping("/database-info")
    public ApiResponse<Map<String, Object>> getDatabaseInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            info.put("databaseUrl", metaData.getURL());
            info.put("databaseProductName", metaData.getDatabaseProductName());
            info.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            info.put("driverName", metaData.getDriverName());
            info.put("driverVersion", metaData.getDriverVersion());
            
            // 获取所有表
            List<Map<String, String>> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    Map<String, String> table = new HashMap<>();
                    table.put("name", rs.getString("TABLE_NAME"));
                    table.put("type", rs.getString("TABLE_TYPE"));
                    tables.add(table);
                }
            }
            info.put("tables", tables);
            info.put("tableCount", tables.size());
            
        } catch (SQLException e) {
            log.error("获取数据库信息失败", e);
            return ApiResponse.error("获取数据库信息失败: " + e.getMessage());
        }
        
        return ApiResponse.success(info);
    }

    /**
     * 获取表统计信息
     */
    @GetMapping("/table-stats")
    public ApiResponse<List<Map<String, Object>>> getTableStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        
        // 定义需要统计的表
        String[] tables = {
            "users", "teachers", "students", "courses", "classes", 
            "class_students", "lessons", "lesson_attendances", 
            "campuses", "announcements", "travel_times", "backup_records"
        };
        
        for (String table : tables) {
            try {
                Query query = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + table);
                Long count = ((Number) query.getSingleResult()).longValue();
                
                Map<String, Object> stat = new HashMap<>();
                stat.put("tableName", table);
                stat.put("recordCount", count);
                stats.add(stat);
            } catch (Exception e) {
                log.warn("统计表 {} 失败: {}", table, e.getMessage());
            }
        }
        
        return ApiResponse.success(stats);
    }

    /**
     * 清空课节数据
     */
    @DeleteMapping("/lessons")
    @Transactional
    public ApiResponse<Void> clearLessons() {
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM batch_schedule_records").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        log.info("课节数据已清空");
        return ApiResponse.success("课节数据已清空", null);
    }

    /**
     * 清空班级数据
     */
    @DeleteMapping("/classes")
    @Transactional
    public ApiResponse<Void> clearClasses() {
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM classes").executeUpdate();
        log.info("班级数据已清空");
        return ApiResponse.success("班级数据已清空", null);
    }

    /**
     * 清空学生数据
     */
    @DeleteMapping("/students")
    @Transactional
    public ApiResponse<Void> clearStudents() {
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM students").executeUpdate();
        log.info("学生数据已清空");
        return ApiResponse.success("学生数据已清空", null);
    }

    /**
     * 清空老师数据
     */
    @DeleteMapping("/teachers")
    @Transactional
    public ApiResponse<Void> clearTeachers() {
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM travel_times").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM teachers").executeUpdate();
        log.info("老师数据已清空");
        return ApiResponse.success("老师数据已清空", null);
    }

    /**
     * 清空课程数据
     */
    @DeleteMapping("/courses")
    @Transactional
    public ApiResponse<Void> clearCourses() {
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM courses").executeUpdate();
        log.info("课程数据已清空");
        return ApiResponse.success("课程数据已清空", null);
    }

    /**
     * 清空校区数据
     */
    @DeleteMapping("/campuses")
    @Transactional
    public ApiResponse<Void> clearCampuses() {
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM classes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM campuses").executeUpdate();
        log.info("校区数据已清空");
        return ApiResponse.success("校区数据已清空", null);
    }

    /**
     * 清空所有业务数据（保留用户和备份配置）
     */
    @DeleteMapping("/all-business")
    @Transactional
    public ApiResponse<Void> clearAllBusiness() {
        // 按依赖顺序删除（从最底层依赖开始）
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM batch_schedule_records").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM classes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM teachers").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM courses").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM campuses").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM announcements").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM travel_times").executeUpdate();
        log.info("所有业务数据已清空");
        return ApiResponse.success("所有业务数据已清空（保留用户和备份配置）", null);
    }

    /**
     * 清空所有数据（危险操作）
     */
    @DeleteMapping("/all")
    @Transactional
    public ApiResponse<Void> clearAll() {
        // 按依赖顺序删除
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM batch_schedule_records").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM classes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM teachers").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM courses").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM campuses").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM announcements").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM travel_times").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM backup_records").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM backup_configs").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM users").executeUpdate();
        log.info("所有数据已清空");
        return ApiResponse.success("所有数据已清空", null);
    }

    /**
     * 执行原生 SQL 查询（只读）
     */
    @PostMapping("/query")
    @Transactional(readOnly = true)
    public ApiResponse<List<Map<String, Object>>> executeQuery(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return ApiResponse.error("SQL 语句不能为空");
        }

        // 安全检查：只允许 SELECT 语句
        String upperSql = sql.trim().toUpperCase();
        if (!upperSql.startsWith("SELECT")) {
            return ApiResponse.error("只允许执行 SELECT 查询语句");
        }

        try {
            Query query = entityManager.createNativeQuery(sql);
            List<Object[]> results = query.getResultList();
            
            // 获取列名
            List<String> columnNames = new ArrayList<>();
            // 列名将在后续版本中通过 ResultSet 元数据获取

            List<Map<String, Object>> formattedResults = new ArrayList<>();
            for (Object row : results) {
                Map<String, Object> rowMap = new HashMap<>();
                if (row instanceof Object[]) {
                    Object[] columns = (Object[]) row;
                    for (int i = 0; i < columns.length; i++) {
                        rowMap.put("column" + i, columns[i]);
                    }
                } else {
                    rowMap.put("result", row);
                }
                formattedResults.add(rowMap);
            }

            log.info("执行查询: {}, 返回 {} 条记录", sql, formattedResults.size());
            return ApiResponse.success("查询成功", formattedResults);
        } catch (Exception e) {
            log.error("执行查询失败: {}", sql, e);
            return ApiResponse.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统健康状态
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        // 数据库连接状态
        try (Connection conn = dataSource.getConnection()) {
            health.put("database", "UP");
            health.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
        }
        
        // JVM 信息
        Runtime runtime = Runtime.getRuntime();
        health.put("jvmTotalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        health.put("jvmFreeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        health.put("jvmMaxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        health.put("availableProcessors", runtime.availableProcessors());
        
        return ApiResponse.success(health);
    }
}
