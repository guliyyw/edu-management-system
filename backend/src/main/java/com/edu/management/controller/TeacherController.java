package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.TeacherDto;
import com.edu.management.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TeacherController {
    
    private final TeacherService teacherService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TeacherDto> create(@RequestBody TeacherDto dto) {
        return ApiResponse.success("创建成功", teacherService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TeacherDto> update(@PathVariable Long id, @RequestBody TeacherDto dto) {
        return ApiResponse.success("更新成功", teacherService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<TeacherDto> getById(@PathVariable Long id) {
        return ApiResponse.success(teacherService.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TeacherDto>> getAll() {
        return ApiResponse.success(teacherService.getAll());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<TeacherDto>> getActiveTeachers() {
        return ApiResponse.success(teacherService.getActiveTeachers());
    }
    
    @GetMapping("/campus/{campusId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<TeacherDto>> getByCampusId(@PathVariable Long campusId) {
        return ApiResponse.success(teacherService.getByCampusId(campusId));
    }
}
