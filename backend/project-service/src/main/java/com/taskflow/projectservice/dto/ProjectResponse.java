package com.taskflow.projectservice.dto;

import com.taskflow.projectservice.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private ProjectStatus status;

    private Long ownerId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}