package com.edu.management.dto;

import com.edu.management.enums.RoleType;
import com.edu.management.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserDto {

    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private RoleType role;
    private UserStatus status;
    private Long teacherId;
    private Long campusId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}
