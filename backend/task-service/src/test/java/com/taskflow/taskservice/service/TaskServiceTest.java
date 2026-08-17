package com.taskflow.taskservice.service;

import com.taskflow.taskservice.client.NotificationServiceClient;
import com.taskflow.taskservice.client.UserServiceClient;
import com.taskflow.taskservice.dto.notification.NotificationRequest;
import com.taskflow.taskservice.dto.request.CreateTaskRequest;
import com.taskflow.taskservice.dto.response.TaskResponse;
import com.taskflow.taskservice.dto.response.UserResponse;
import com.taskflow.taskservice.entity.Task;
import com.taskflow.taskservice.enums.Priority;
import com.taskflow.taskservice.enums.TaskStatus;
import com.taskflow.taskservice.exception.TaskNotFoundException;
import com.taskflow.taskservice.repository.TaskRepository;
import com.taskflow.taskservice.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TaskServiceImpl service;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Test
    void shouldCreateTaskSuccessfully() {

        CreateTaskRequest request = new CreateTaskRequest();

        request.setTitle("Backend API");
        request.setDescription("Develop REST API");
        request.setStatus(TaskStatus.TODO);
        request.setPriority(Priority.HIGH);
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setProjectId(1L);
        request.setAssignedUserId(1L);

        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setName("Shreyas");
        user.setEmail("test@test.com");
        user.setRole("USER");

        when(userServiceClient.getUserById(1L))
                .thenReturn(user);

        Task task = Task.builder()
                .id(1L)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .projectId(request.getProjectId())
                .assignedUserId(request.getAssignedUserId())
                .build();

        when(repository.save(any(Task.class)))
                .thenReturn(task);

        doNothing().when(notificationServiceClient)
                .createNotification(any(NotificationRequest.class));

        TaskResponse response = service.createTask(request);

        assertNotNull(response);
        assertEquals("Backend API", response.getTitle());

        verify(userServiceClient, times(1)).getUserById(1L);
        verify(repository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldReturnTaskById() {

        Task task = Task.builder()
                .id(1L)
                .title("Backend API")
                .description("REST API")
                .status(TaskStatus.TODO)
                .priority(Priority.HIGH)
                .dueDate(LocalDate.now().plusDays(3))
                .projectId(1L)
                .assignedUserId(1L)
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponse response = service.getTaskById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Backend API", response.getTitle());

        verify(repository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {

        when(repository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> service.getTaskById(99L)
        );
    }

    @Test
    void shouldDeleteTaskSuccessfully() {

        Task task = Task.builder()
                .id(1L)
                .title("Task")
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(task));

        service.deleteTask(1L);

        verify(repository).delete(task);
    }

}