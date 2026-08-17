package com.taskflow.taskservice.controller;

import com.taskflow.taskservice.dto.request.CreateTaskRequest;
import com.taskflow.taskservice.dto.request.UpdateTaskRequest;
import com.taskflow.taskservice.dto.response.TaskResponse;
import com.taskflow.taskservice.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create Task")
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        return new ResponseEntity<>(
                taskService.createTask(request),
                HttpStatus.CREATED
        );
    }

    @Operation(summary = "Get All Tasks")
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {

        return ResponseEntity.ok(
                taskService.getAllTasks()
        );
    }

    @Operation(summary = "Get Task By ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }

    @Operation(summary = "Update Task")
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {

        return ResponseEntity.ok(
                taskService.updateTask(id, request)
        );
    }

    @Operation(summary = "Delete Task")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id) {

        taskService.deleteTask(id);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Tasks By Project")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                taskService.getTasksByProject(projectId)
        );
    }

    @Operation(summary = "Get Tasks By Assigned User")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksByAssignedUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                taskService.getTasksByAssignedUser(userId)
        );
    }

    @Operation(summary = "Get Tasks with Pagination")
    @GetMapping("/page")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "id") String sortBy) {

        return ResponseEntity.ok(
                taskService.getAllTasks(page, size, sortBy)
        );
    }

    @Operation(summary = "Search Tasks")
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> searchTasks(

            @RequestParam String keyword) {

        return ResponseEntity.ok(
                taskService.searchTasks(keyword)
        );
    }
}