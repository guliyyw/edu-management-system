package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.LessonDto;
import com.edu.management.enums.AttendanceStatus;
import com.edu.management.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LessonController {
    
    private final LessonService lessonService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<LessonDto> create(@RequestBody LessonDto dto) {
        return ApiResponse.success("创建成功", lessonService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<LessonDto> update(@PathVariable Long id, @RequestBody LessonDto dto) {
        return ApiResponse.success("更新成功", lessonService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        lessonService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<LessonDto> getById(@PathVariable Long id) {
        return ApiResponse.success(lessonService.getById(id));
    }
    
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<LessonDto>> getByClassId(@PathVariable Long classId) {
        return ApiResponse.success(lessonService.getByClassId(classId));
    }
    
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<LessonDto>> getByTeacherIdAndDate(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(lessonService.getByTeacherIdAndDate(teacherId, date));
    }
    
    @GetMapping("/teacher/{teacherId}/range")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<LessonDto>> getByTeacherIdAndDateRange(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(lessonService.getByTeacherIdAndDateRange(teacherId, startDate, endDate));
    }
    
    @GetMapping("/campus/{campusId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<LessonDto>> getByCampusIdAndDate(
            @PathVariable Long campusId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(lessonService.getByCampusIdAndDate(campusId, date));
    }
    
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<LessonDto>> generateLessons(
            @RequestParam Long classId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success("生成成功", lessonService.generateLessons(classId, startDate, endDate));
    }
    
    @PostMapping("/{lessonId}/attendance/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<Void> markAttendance(
            @PathVariable Long lessonId,
            @PathVariable Long studentId,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remark) {
        lessonService.markAttendance(lessonId, studentId, status, remark);
        return ApiResponse.success("签到成功", null);
    }
    
    @PostMapping("/{lessonId}/attendance/{studentId}/modify")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> modifyAttendance(
            @PathVariable Long lessonId,
            @PathVariable Long studentId,
            @RequestParam AttendanceStatus status,
            @RequestParam(required = false) String remark,
            @RequestParam String modifyReason,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 获取当前用户ID
        Long modifiedBy = 1L;
        lessonService.modifyAttendance(lessonId, studentId, status, remark, modifyReason, modifiedBy);
        return ApiResponse.success("修改成功", null);
    }
}
