package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.ClassDto;
import com.edu.management.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ClassController {
    
    private final ClassService classService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<ClassDto> create(@RequestBody ClassDto dto) {
        return ApiResponse.success("创建成功", classService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<ClassDto> update(@PathVariable Long id, @RequestBody ClassDto dto) {
        return ApiResponse.success("更新成功", classService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        classService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<ClassDto> getById(@PathVariable Long id) {
        return ApiResponse.success(classService.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ClassDto>> getAll() {
        return ApiResponse.success(classService.getAll());
    }
    
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<ClassDto>> getByTeacherId(@PathVariable Long teacherId) {
        return ApiResponse.success(classService.getByTeacherId(teacherId));
    }
    
    @GetMapping("/campus/{campusId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ClassDto>> getByCampusId(@PathVariable Long campusId) {
        return ApiResponse.success(classService.getByCampusId(campusId));
    }
    
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ClassDto>> getByStudentId(@PathVariable Long studentId) {
        return ApiResponse.success(classService.getByStudentId(studentId));
    }
    
    @PostMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> addStudentToClass(@PathVariable Long classId, 
                                                @PathVariable Long studentId,
                                                @RequestParam(defaultValue = "false") Boolean isTrial) {
        classService.addStudentToClass(classId, studentId, isTrial);
        return ApiResponse.success("添加成功", null);
    }
    
    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> removeStudentFromClass(@PathVariable Long classId, @PathVariable Long studentId) {
        classService.removeStudentFromClass(classId, studentId);
        return ApiResponse.success("移除成功", null);
    }
    
    @PostMapping("/{classId}/students/{studentId}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> convertTrialStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        classService.convertTrialStudent(classId, studentId);
        return ApiResponse.success("转正成功", null);
    }
}
