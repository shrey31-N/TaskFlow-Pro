package com.taskflow.taskservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.taskservice.client.UserServiceClient;
import com.taskflow.taskservice.dto.request.CreateTaskRequest;
import com.taskflow.taskservice.dto.response.UserResponse;
import com.taskflow.taskservice.enums.Priority;
import com.taskflow.taskservice.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldCreateTask() throws Exception {

        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setName("Shreyas");
        user.setEmail("test@test.com");
        user.setRole("USER");

        when(userServiceClient.getUserById(1L))
                .thenReturn(user);

        CreateTaskRequest request = new CreateTaskRequest();

        request.setTitle("Integration Test");
        request.setDescription("Testing Task API");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(Priority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setProjectId(1L);
        request.setAssignedUserId(1L);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetTaskById() throws Exception {

        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setName("Shreyas");
        user.setEmail("test@test.com");
        user.setRole("USER");

        when(userServiceClient.getUserById(1L))
                .thenReturn(user);

        CreateTaskRequest request = new CreateTaskRequest();

        request.setTitle("Task One");
        request.setDescription("Integration Test");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(Priority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(3));
        request.setProjectId(1L);
        request.setAssignedUserId(1L);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {

        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsEmpty() throws Exception {

        CreateTaskRequest request = new CreateTaskRequest();

        request.setTitle("");
        request.setDescription("Test");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}