package com.taskflow.userservice.dto.request;

//import com.taskflow.userservice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import com.taskflow.userservice.enums.Role;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$",
            message = "Password must contain uppercase, lowercase, number and minimum 8 characters"
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private com.taskflow.userservice.enums.Role role;
}