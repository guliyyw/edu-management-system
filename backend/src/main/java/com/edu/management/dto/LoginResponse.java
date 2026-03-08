package com.edu.management.dto;

import com.edu.management.enums.RoleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    
    private String token;
    private String username;
    private String realName;
    private RoleType role;
    private Long teacherId;
    private Long campusId;
}
