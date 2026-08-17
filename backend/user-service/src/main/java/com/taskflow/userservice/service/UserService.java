package com.taskflow.userservice.service;

import com.taskflow.userservice.dto.request.ChangePasswordRequest;
import com.taskflow.userservice.dto.request.CreateUserRequest;
import com.taskflow.userservice.dto.request.LoginRequest;
import com.taskflow.userservice.dto.request.UpdateUserRequest;
import com.taskflow.userservice.dto.response.LoginResponse;
import com.taskflow.userservice.dto.response.MessageResponse;
import com.taskflow.userservice.dto.response.UserResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    UserResponse register(CreateUserRequest request);

    LoginResponse login(LoginRequest request);

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateUserRequest request);

    MessageResponse changePassword(ChangePasswordRequest request);

    UserResponse uploadProfileImage(MultipartFile file);

    UserResponse getUserById(Long id);
}