package com.edu.management.controller;

import com.edu.management.dto.ApiResponse;
import com.edu.management.dto.TravelTimeDto;
import com.edu.management.service.TravelTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/travel-times")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TravelTimeController {
    
    private final TravelTimeService travelTimeService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<TravelTimeDto> create(@RequestBody TravelTimeDto dto) {
        return ApiResponse.success("创建成功", travelTimeService.create(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<TravelTimeDto> update(@PathVariable Long id, @RequestBody TravelTimeDto dto) {
        return ApiResponse.success("更新成功", travelTimeService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        travelTimeService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<TravelTimeDto> getById(@PathVariable Long id) {
        return ApiResponse.success(travelTimeService.getById(id));
    }
    
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<TravelTimeDto>> getByTeacherId(@PathVariable Long teacherId) {
        return ApiResponse.success(travelTimeService.getByTeacherId(teacherId));
    }
    
    @GetMapping("/teacher/{teacherId}/effective")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<TravelTimeDto> getEffectiveTravelTime(
            @PathVariable Long teacherId,
            @RequestParam Long fromCampusId,
            @RequestParam Long toCampusId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(travelTimeService.getEffectiveTravelTime(teacherId, fromCampusId, toCampusId, date));
    }
}
