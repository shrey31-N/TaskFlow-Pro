package com.taskflow.projectservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.projectservice.dto.CreateProjectRequest;
import com.taskflow.projectservice.enums.ProjectStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateProjectSuccessfully() throws Exception {

        CreateProjectRequest request = new CreateProjectRequest();

        request.setName("TaskFlow-Pro");
        request.setDescription("Integration Test");
        request.setStatus(ProjectStatus.ACTIVE);
        request.setOwnerId(1L);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

    }
}