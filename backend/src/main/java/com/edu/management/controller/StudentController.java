package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.ClassDto;
import com.edu.management.dto.StudentDto;
import com.edu.management.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<StudentDto> create(@RequestBody StudentDto dto) {
        return ApiResponse.success("创建成功", studentService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<StudentDto> update(@PathVariable Long id, @RequestBody StudentDto dto) {
        return ApiResponse.success("更新成功", studentService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<StudentDto> getById(@PathVariable Long id) {
        return ApiResponse.success(studentService.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<StudentDto>> getAll() {
        return ApiResponse.success(studentService.getAll());
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<StudentDto>> search(@RequestParam String keyword) {
        return ApiResponse.success(studentService.search(keyword));
    }

    @GetMapping("/{id}/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ClassDto>> getStudentClasses(@PathVariable Long id) {
        return ApiResponse.success(studentService.getStudentClasses(id));
    }
}
