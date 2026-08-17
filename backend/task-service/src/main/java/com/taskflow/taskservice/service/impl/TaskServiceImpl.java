package com.taskflow.taskservice.service.impl;

import com.taskflow.taskservice.client.NotificationServiceClient;
import com.taskflow.taskservice.client.UserServiceClient;
import com.taskflow.taskservice.dto.notification.NotificationRequest;
import com.taskflow.taskservice.dto.request.CreateTaskRequest;
import com.taskflow.taskservice.dto.request.UpdateTaskRequest;
import com.taskflow.taskservice.dto.response.TaskResponse;
import com.taskflow.taskservice.dto.response.UserResponse;
import com.taskflow.taskservice.entity.Task;
import com.taskflow.taskservice.exception.ServiceUnavailableException;
import com.taskflow.taskservice.exception.TaskNotFoundException;
import com.taskflow.taskservice.repository.TaskRepository;
import com.taskflow.taskservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;


    @CircuitBreaker(name = "userService", fallbackMethod = "userServiceFallback")
    @Override
    public TaskResponse createTask(CreateTaskRequest request) {

        UserResponse user =
                userServiceClient.getUserById(request.getAssignedUserId());

        if (user == null) {
            throw new RuntimeException("Assigned user not found");
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus())
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .projectId(request.getProjectId())
                .assignedUserId(request.getAssignedUserId())
                .build();

        Task savedTask = repository.save(task);
        NotificationRequest notification = new NotificationRequest();

        notification.setUserId(savedTask.getAssignedUserId());

        notification.setTitle("Task Assigned");

        notification.setMessage(
                "A new task '" + savedTask.getTitle() + "' has been assigned to you."
        );

        notification.setType("IN_APP");

        notificationServiceClient.createNotification(notification);

        return mapToResponse(savedTask);
    }

    @Override
    public List<TaskResponse> getAllTasks() {

        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public TaskResponse getTaskById(Long id) {

        Task task = repository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found"));

        return mapToResponse(task);
    }

    @Override
    public TaskResponse updateTask(Long id,
                                   UpdateTaskRequest request) {

        Task task = repository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found"));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setProjectId(request.getProjectId());
        task.setAssignedUserId(request.getAssignedUserId());

        Task updatedTask = repository.save(task);

        return mapToResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {

        Task task = repository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task not found"));

        repository.delete(task);
    }

    @Override
    public List<TaskResponse> getTasksByProject(Long projectId) {

        return repository.findByProjectId(projectId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<TaskResponse> getTasksByAssignedUser(Long assignedUserId) {

        return repository.findByAssignedUserId(assignedUserId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TaskResponse mapToResponse(Task task) {

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .projectId(task.getProjectId())
                .assignedUserId(task.getAssignedUserId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    @Override
    public Page<TaskResponse> getAllTasks(int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortBy).ascending()
        );

        return repository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<TaskResponse> searchTasks(String keyword) {

        return repository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TaskResponse userServiceFallback(
            CreateTaskRequest request,
            Exception ex) {

        throw new ServiceUnavailableException(
                "User Service is temporarily unavailable. Please try again later."
        );
    }
}