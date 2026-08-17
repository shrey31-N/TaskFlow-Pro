package com.taskflow.taskservice.service;

import com.taskflow.taskservice.dto.request.CreateTaskRequest;
import com.taskflow.taskservice.dto.request.UpdateTaskRequest;
import com.taskflow.taskservice.dto.response.TaskResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);

    List<TaskResponse> getAllTasks();

    TaskResponse getTaskById(Long id);

    TaskResponse updateTask(Long id, UpdateTaskRequest request);

    void deleteTask(Long id);

    List<TaskResponse> getTasksByProject(Long projectId);

    List<TaskResponse> getTasksByAssignedUser(Long assignedUserId);

    Page<TaskResponse> getAllTasks(int page, int size, String sortBy);

    List<TaskResponse> searchTasks(String keyword);
}