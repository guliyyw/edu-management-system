package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.ClassDto;
import com.edu.management.service.ClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
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
        log.info("[班级API] 获取班级详情，ID: {}", id);
        ClassDto dto = classService.getById(id);
        log.info("[班级API] 返回班级详情，ID: {}，学生数量: {}", id, dto.getStudents() != null ? dto.getStudents().size() : 0);
        return ApiResponse.success(dto);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<ClassDto>> getAll() {
        log.info("[班级API] 获取所有班级");
        List<ClassDto> list = classService.getAll();
        log.info("[班级API] 返回班级数量: {}", list.size());
        return ApiResponse.success(list);
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
        log.info("[班级API] 添加学生到班级，班级ID: {}，学生ID: {}，是否试课: {}", classId, studentId, isTrial);
        classService.addStudentToClass(classId, studentId, isTrial);
        log.info("[班级API] 添加学生成功，班级ID: {}，学生ID: {}", classId, studentId);
        return ApiResponse.success("添加成功", null);
    }

    @DeleteMapping("/{classId}/students/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> removeStudentFromClass(@PathVariable Long classId, @PathVariable Long studentId) {
        log.info("[班级API] 从班级移除学生，班级ID: {}，学生ID: {}", classId, studentId);
        classService.removeStudentFromClass(classId, studentId);
        log.info("[班级API] 移除学生成功，班级ID: {}，学生ID: {}", classId, studentId);
        return ApiResponse.success("移除成功", null);
    }

    @PostMapping("/{classId}/students/{studentId}/convert")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> convertTrialStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        classService.convertTrialStudent(classId, studentId);
        return ApiResponse.success("转正成功", null);
    }
}
