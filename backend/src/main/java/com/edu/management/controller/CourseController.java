package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.CourseDto;
import com.edu.management.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CourseController {
    
    private final CourseService courseService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseDto> create(@RequestBody CourseDto dto) {
        return ApiResponse.success("创建成功", courseService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CourseDto> update(@PathVariable Long id, @RequestBody CourseDto dto) {
        return ApiResponse.success("更新成功", courseService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        courseService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<CourseDto> getById(@PathVariable Long id) {
        return ApiResponse.success(courseService.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<CourseDto>> getAll() {
        return ApiResponse.success(courseService.getAll());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<CourseDto>> getActiveCourses() {
        return ApiResponse.success(courseService.getActiveCourses());
    }
}
