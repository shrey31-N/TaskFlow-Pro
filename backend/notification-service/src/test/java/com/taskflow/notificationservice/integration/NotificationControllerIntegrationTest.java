package com.taskflow.notificationservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskflow.notificationservice.dto.request.CreateNotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void shouldCreateNotification() throws Exception {

        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setUserId(1L);
        request.setTitle("Integration Test");
        request.setMessage("Testing Notification API");
        request.setType("IN_APP");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetNotificationById() throws Exception {

        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setUserId(1L);
        request.setTitle("Notification");
        request.setMessage("Testing");
        request.setType("EMAIL");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifications/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenNotificationNotFound() throws Exception {

        mockMvc.perform(get("/api/notifications/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsEmpty() throws Exception {

        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setUserId(1L);
        request.setTitle("");
        request.setMessage("Testing");
        request.setType("EMAIL");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteNotification() throws Exception {

        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setUserId(1L);
        request.setTitle("Delete Test");
        request.setMessage("Delete Notification");
        request.setType("EMAIL");

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isNoContent());
    }

}