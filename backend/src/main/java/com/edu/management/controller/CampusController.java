package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.CampusDto;
import com.edu.management.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CampusController {
    
    private final CampusService campusService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampusDto> create(@RequestBody CampusDto dto) {
        return ApiResponse.success("创建成功", campusService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CampusDto> update(@PathVariable Long id, @RequestBody CampusDto dto) {
        return ApiResponse.success("更新成功", campusService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        campusService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<CampusDto> getById(@PathVariable Long id) {
        return ApiResponse.success(campusService.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<CampusDto>> getAll() {
        return ApiResponse.success(campusService.getAll());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<CampusDto>> getActiveCampuses() {
        return ApiResponse.success(campusService.getActiveCampuses());
    }
}
