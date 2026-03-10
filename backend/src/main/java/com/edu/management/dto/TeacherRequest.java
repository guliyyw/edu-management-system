package com.edu.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class TeacherRequest {

    @NotBlank(message = "老师姓名不能为空")
    private String name;

    @NotBlank(message = "电话不能为空")
    private String phone;

    @NotEmpty(message = "请至少选择一个校区")
    private List<Long> campusIds;
}
