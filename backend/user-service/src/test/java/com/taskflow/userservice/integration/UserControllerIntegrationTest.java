package com.taskflow.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.userservice.dto.request.CreateUserRequest;
import com.taskflow.userservice.dto.request.LoginRequest;
import com.taskflow.userservice.enums.Role;
import com.taskflow.userservice.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private S3Service s3Service;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        CreateUserRequest request = new CreateUserRequest();

        request.setFullName("Shreyas");
        request.setEmail("shreyas@test.com");
        request.setPassword("Password@123");
        request.setPhone("9876543210");
        request.setRole(Role.MEMBER);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Shreyas"))
                .andExpect(jsonPath("$.email").value("shreyas@test.com"))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.role").value("MEMBER"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {

        CreateUserRequest request = new CreateUserRequest();
        request.setFullName("Shreyas");
        request.setEmail("shreyas@test.com");
        request.setPassword("Password@123");
        request.setPhone("9876543210");
        request.setRole(Role.MEMBER);

        // First registration
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Second registration with same email
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
    @Test
    void shouldLoginSuccessfully() throws Exception {

        // Register user
        CreateUserRequest register = new CreateUserRequest();
        register.setFullName("Shreyas");
        register.setEmail("login@test.com");
        register.setPassword("Password@123");
        register.setPhone("9876543210");
        register.setRole(Role.MEMBER);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // Login
        LoginRequest login = new LoginRequest();
        login.setEmail("login@test.com");
        login.setPassword("Password@123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"));
    }
    @Test
    void shouldFailLoginWithInvalidPassword() throws Exception {

        CreateUserRequest register = new CreateUserRequest();
        register.setFullName("Shreyas");
        register.setEmail("wrong@test.com");
        register.setPassword("Password@123");
        register.setPhone("9876543210");
        register.setRole(Role.MEMBER);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequest login = new LoginRequest();
        login.setEmail("wrong@test.com");
        login.setPassword("WrongPassword");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

}