package com.edu.management.service;

import com.edu.management.dto.LoginRequest;
import com.edu.management.dto.LoginResponse;

public interface AuthService {
    
    LoginResponse login(LoginRequest request);
    
    void logout(String token);
    
    boolean validateToken(String token);
}
