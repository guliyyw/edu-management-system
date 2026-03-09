package com.edu.management.config;

import com.edu.management.entity.*;
import com.edu.management.enums.*;
import com.edu.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否已存在管理员账号
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        // 只初始化管理员账号
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRealName("系统管理员");
        admin.setPhone("13800000000");
        admin.setRole(RoleType.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        userRepository.save(admin);

        System.out.println("===== 系统初始化完成 =====");
        System.out.println("管理员账号: admin / admin123");
        System.out.println("请使用管理员账号登录后创建其他数据");
    }
}
