package com.edu.management.service.impl;

import com.edu.management.dto.LoginRequest;
import com.edu.management.dto.LoginResponse;
import com.edu.management.entity.User;
import com.edu.management.enums.UserStatus;
import com.edu.management.repository.UserRepository;
import com.edu.management.security.JwtTokenProvider;
import com.edu.management.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameAndStatus(request.getUsername(), UserStatus.ACTIVE)
                .orElseThrow(() -> new BadCredentialsException("用户名或密码错误"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = jwtTokenProvider.generateToken(user);
        
        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .teacherId(user.getTeacherId())
                .campusId(user.getCampusId())
                .build();
    }
    
    @Override
    public void logout(String token) {
        // 可以在这里实现token黑名单逻辑
        jwtTokenProvider.invalidateToken(token);
    }
    
    @Override
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}
