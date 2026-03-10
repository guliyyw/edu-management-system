package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DebugController {

    @PersistenceContext
    private EntityManager entityManager;

    @DeleteMapping("/lessons")
    @Transactional
    public ApiResponse<Void> clearLessons() {
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM batch_schedule_records").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        return ApiResponse.success("课节数据已清空", null);
    }

    @DeleteMapping("/classes")
    @Transactional
    public ApiResponse<Void> clearClasses() {
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM classes").executeUpdate();
        return ApiResponse.success("班级数据已清空", null);
    }

    @DeleteMapping("/students")
    @Transactional
    public ApiResponse<Void> clearStudents() {
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM students").executeUpdate();
        return ApiResponse.success("学生数据已清空", null);
    }

    @DeleteMapping("/teachers")
    @Transactional
    public ApiResponse<Void> clearTeachers() {
        entityManager.createNativeQuery("DELETE FROM teachers").executeUpdate();
        return ApiResponse.success("老师数据已清空", null);
    }

    @DeleteMapping("/all")
    @Transactional
    public ApiResponse<Void> clearAll() {
        // 按依赖顺序删除（从最底层依赖开始）
        entityManager.createNativeQuery("DELETE FROM lesson_attendances").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM batch_schedule_records").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM lessons").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM class_students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM classes").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM students").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM teachers").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM courses").executeUpdate();
        return ApiResponse.success("所有业务数据已清空", null);
    }
}
