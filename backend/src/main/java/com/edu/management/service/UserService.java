package com.edu.management.service;

import com.edu.management.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long id);

    UserDto createUser(UserDto dto);

    UserDto updateUser(Long id, UserDto dto);

    void deleteUser(Long id);
}
