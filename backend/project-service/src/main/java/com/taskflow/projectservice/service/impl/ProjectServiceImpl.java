package com.taskflow.projectservice.service.impl;

import com.taskflow.projectservice.dto.CreateProjectRequest;
import com.taskflow.projectservice.dto.ProjectResponse;
import com.taskflow.projectservice.dto.UpdateProjectRequest;
import com.taskflow.projectservice.entity.Project;
import com.taskflow.projectservice.exception.ProjectNotFoundException;
import com.taskflow.projectservice.repository.ProjectRepository;
import com.taskflow.projectservice.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .status(request.getStatus())
                .ownerId(request.getOwnerId())
                .build();

        Project saved = repository.save(project);
        log.info("Creating project: {}", request.getName());
        log.info("Project created successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    public
    Page<ProjectResponse> getAllProjects(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return repository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {

        Project project = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        log.info("Fetching project with ID: {}", id);

        return mapToResponse(project);
    }

    @Override
    public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {

        Project project = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());

        Project updated = repository.save(project);
        log.info("Updating project with ID: {}", id);

        return mapToResponse(updated);
    }

    @Override
    public void deleteProject(Long id) {

        Project project = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        log.info("Deleting project with ID: {}", id);

        repository.delete(project);
    }

    @Override
    public List<ProjectResponse> searchProjects(String name) {

        log.info("Searching project with name {}", name);

        return repository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ProjectResponse mapToResponse(Project project) {

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .status(project.getStatus())
                .ownerId(project.getOwnerId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }


}