package com.taskflow.userservice.service.impl;

import com.taskflow.userservice.dto.request.ChangePasswordRequest;
import com.taskflow.userservice.dto.request.CreateUserRequest;
import com.taskflow.userservice.dto.request.LoginRequest;
import com.taskflow.userservice.dto.request.UpdateUserRequest;
import com.taskflow.userservice.dto.response.LoginResponse;
import com.taskflow.userservice.dto.response.MessageResponse;
import com.taskflow.userservice.dto.response.UserResponse;
import com.taskflow.userservice.entity.User;
//import com.taskflow.userservice.enums.Role;
import com.taskflow.userservice.exception.EmailAlreadyExistsException;
import com.taskflow.userservice.exception.InvalidPasswordException;
import com.taskflow.userservice.exception.UserNotFoundException;
import com.taskflow.userservice.repository.UserRepository;
import com.taskflow.userservice.security.JwtService;
import com.taskflow.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.taskflow.userservice.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskflow.userservice.exception.UserNotFoundException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final S3Service s3Service;
    private static final Logger logger =
            LoggerFactory.getLogger(UserServiceImpl.class);
    //private Role role;

    @Override
    public UserResponse register(CreateUserRequest request) {

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());

        // Set role from request
        user.setRole(request.getRole());

        User savedUser = repository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().name())
                .profileImageUrl(savedUser.getProfileImageUrl())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(3600000L)
                .build();
    }
    
    @Override
    public UserResponse getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    @Override
    public UserResponse updateProfile(UpdateUserRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());

        User updatedUser = repository.save(user);

        return mapToResponse(updatedUser);
    }

    private UserResponse mapToResponse(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
    @Override
    public MessageResponse changePassword(ChangePasswordRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = repository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new InvalidPasswordException("Current password is incorrect");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new RuntimeException("New password and confirm password do not match");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        repository.save(user);

        return MessageResponse.builder()
                .message("Password changed successfully")
                .build();
    }
    @Override
    public UserResponse uploadProfileImage(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Please select an image to upload.");
        }

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ===============================
        // Delete previous profile image
        // ===============================
        System.out.println("===== Upload Profile Image =====");

        logger.info("Old image URL: {}", user.getProfileImageUrl());

        if (user.getProfileImageUrl() != null &&
                !user.getProfileImageUrl().isBlank()) {

            String oldKey = s3Service.extractKeyFromUrl(user.getProfileImageUrl());

            System.out.println("Old Key: " + oldKey);

            s3Service.deleteFile(oldKey);

            System.out.println("Old image deleted successfully.");
        }

        logger.info("Uploading new profile image.");

        String imageUrl = s3Service.uploadFile(file);

        System.out.println("New Image URL: " + imageUrl);

        // Save new image URL
        user.setProfileImageUrl(imageUrl);

        User updatedUser = repository.save(user);

        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse getUserById(Long id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with id: " + id));

        return mapToResponse(user);
    }

}