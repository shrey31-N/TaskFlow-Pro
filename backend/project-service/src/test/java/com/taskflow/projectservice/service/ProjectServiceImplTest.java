package com.taskflow.projectservice.service;

import com.taskflow.projectservice.dto.UpdateProjectRequest;
import com.taskflow.projectservice.exception.ProjectNotFoundException;
import com.taskflow.projectservice.repository.ProjectRepository;
import com.taskflow.projectservice.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import com.taskflow.projectservice.dto.CreateProjectRequest;
import com.taskflow.projectservice.dto.ProjectResponse;
import com.taskflow.projectservice.entity.Project;
import com.taskflow.projectservice.enums.ProjectStatus;
import com.taskflow.projectservice.repository.ProjectRepository;
import com.taskflow.projectservice.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository repository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {

    }
    @Test
    void shouldCreateProject() {

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("TaskFlow-Pro");
        request.setDescription("DevOps Project");
        request.setStatus(ProjectStatus.ACTIVE);
        request.setOwnerId(1L);

        Project savedProject = Project.builder()
                .id(1L)
                .name("TaskFlow-Pro")
                .description("DevOps Project")
                .status(ProjectStatus.ACTIVE)
                .ownerId(1L)
                .build();

        when(repository.save(any(Project.class)))
                .thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TaskFlow-Pro", response.getName());
        assertEquals(ProjectStatus.ACTIVE, response.getStatus());
    }

    @Test
    void shouldReturnProjectById() {

        Project project = Project.builder()
                .id(1L)
                .name("TaskFlow-Pro")
                .description("DevOps Project")
                .status(ProjectStatus.ACTIVE)
                .ownerId(1L)
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("TaskFlow-Pro", response.getName());
        assertEquals(ProjectStatus.ACTIVE, response.getStatus());
    }

    @Test
    void shouldThrowProjectNotFoundException() {

        when(repository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> projectService.getProjectById(1L)
        );
    }

    @Test
    void shouldReturnAllProjects() {

        Project project1 = Project.builder()
                .id(1L)
                .name("TaskFlow-Pro")
                .description("DevOps Project")
                .status(ProjectStatus.ACTIVE)
                .ownerId(1L)
                .build();

        Project project2 = Project.builder()
                .id(2L)
                .name("E-Commerce")
                .description("Shopping Project")
                .status(ProjectStatus.COMPLETED)
                .ownerId(2L)
                .build();

        Page<Project> page = new PageImpl<>(List.of(project1, project2));

        when(repository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<ProjectResponse> response =
                projectService.getAllProjects(0, 10);

        assertEquals(2, response.getTotalElements());
    }

    @Test
    void shouldUpdateProject() {

        Project project = Project.builder()
                .id(1L)
                .name("Old Project")
                .description("Old Description")
                .status(ProjectStatus.ACTIVE)
                .ownerId(1L)
                .build();

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Updated Project");
        request.setDescription("Updated Description");
        request.setStatus(ProjectStatus.COMPLETED);

        when(repository.findById(1L))
                .thenReturn(Optional.of(project));

        when(repository.save(any(Project.class)))
                .thenReturn(project);

        ProjectResponse response =
                projectService.updateProject(1L, request);

        assertEquals("Updated Project", response.getName());
        assertEquals(ProjectStatus.COMPLETED, response.getStatus());
    }
    @Test
    void shouldDeleteProject() {

        Project project = Project.builder()
                .id(1L)
                .name("TaskFlow")
                .status(ProjectStatus.ACTIVE)
                .ownerId(1L)
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(project));

        doNothing().when(repository).delete(project);

        projectService.deleteProject(1L);

        verify(repository).delete(project);
    }

    @Test
    void shouldSearchProjects() {

        Project project = Project.builder()
                .id(1L)
                .name("TaskFlow-Pro")
                .description("DevOps")
                .status(ProjectStatus.ACTIVE)
                .ownerId(1L)
                .build();

        when(repository.findByNameContainingIgnoreCase("Task"))
                .thenReturn(List.of(project));

        List<ProjectResponse> response =
                projectService.searchProjects("Task");

        assertEquals(1, response.size());
        assertEquals("TaskFlow-Pro", response.get(0).getName());
    }



}