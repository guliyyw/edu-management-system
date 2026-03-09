package com.edu.management.dto;

import com.edu.management.enums.CampusStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CampusDto {

    private Long id;

    @NotBlank(message = "校区名称不能为空")
    private String name;

    private String address;

    private CampusStatus status;
    private LocalDateTime createdAt;
}
