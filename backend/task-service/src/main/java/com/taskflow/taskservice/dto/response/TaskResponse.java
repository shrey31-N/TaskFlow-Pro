package com.taskflow.taskservice.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private String status;

    private String priority;

    private LocalDate dueDate;

    private Long projectId;

    private Long assignedUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}