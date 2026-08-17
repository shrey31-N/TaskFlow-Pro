package com.taskflow.userservice.controller;

import com.taskflow.userservice.dto.request.CreateUserRequest;
import com.taskflow.userservice.dto.request.LoginRequest;
import com.taskflow.userservice.dto.response.LoginResponse;
import com.taskflow.userservice.dto.response.UserResponse;
import com.taskflow.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.taskflow.userservice.dto.request.UpdateUserRequest;
import com.taskflow.userservice.dto.request.ChangePasswordRequest;
import com.taskflow.userservice.dto.response.MessageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "User Management",
        description = "APIs for User Registration, Authentication and Profile Management"
)
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register User",
            description = "Creates a new user account."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "409", description = "Email already exists"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })

    @PostMapping("/register")
    public UserResponse register(
            @Valid @RequestBody CreateUserRequest request) {

        return userService.register(request);
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a user and returns a JWT token."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(userService.login(request));
    }
    @Operation(
            summary = "Get Current User Profile",
            description = "Returns the profile details of the authenticated user."
    )

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile fetched successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {

        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @Operation(
            summary = "Update User Profile",
            description = "Updates the full name and phone number of the authenticated user."
    )
    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UpdateUserRequest request) {

        return ResponseEntity.ok(userService.updateProfile(request));
    }

    @Operation(
            summary = "Change Password",
            description = "Changes the password of the authenticated user."
    )
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        return ResponseEntity.ok(userService.changePassword(request));
    }

    @Operation(
            summary = "Upload Profile Image",
            description = "Uploads the authenticated user's profile image to Amazon S3."
    )
    @PostMapping(
            value = "/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<UserResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {

        System.out.println("========== Upload API Called ==========");

        return ResponseEntity.ok(userService.uploadProfileImage(file));
    }

    @Operation(
            summary = "Get User By ID",
            description = "Returns user details by user ID."
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }


}

