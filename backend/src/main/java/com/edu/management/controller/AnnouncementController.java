package com.edu.management.controller;

import com.edu.management.dto.AnnouncementDto;
import com.edu.management.dto.ApiResponse;
import com.edu.management.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<AnnouncementDto> create(@RequestBody AnnouncementDto dto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        Long createdBy = Long.valueOf(userDetails.getUsername());
        return ApiResponse.success("创建成功", announcementService.create(dto, createdBy));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<AnnouncementDto> update(@PathVariable Long id, @RequestBody AnnouncementDto dto) {
        return ApiResponse.success("更新成功", announcementService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.success("删除成功", null);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<AnnouncementDto> getById(@PathVariable Long id) {
        return ApiResponse.success(announcementService.getById(id));
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<AnnouncementDto>> getAll() {
        return ApiResponse.success(announcementService.getAll());
    }
    
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<AnnouncementDto>> getActiveAnnouncements() {
        return ApiResponse.success(announcementService.getActiveAnnouncements());
    }
    
    @GetMapping("/top5")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'TEACHER')")
    public ApiResponse<List<AnnouncementDto>> getTop5ActiveAnnouncements() {
        return ApiResponse.success(announcementService.getTop5ActiveAnnouncements());
    }
}
