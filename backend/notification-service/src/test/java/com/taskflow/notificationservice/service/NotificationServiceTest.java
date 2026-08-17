package com.taskflow.notificationservice.service;

import com.taskflow.notificationservice.dto.request.CreateNotificationRequest;
import com.taskflow.notificationservice.dto.response.NotificationResponse;
import com.taskflow.notificationservice.entity.Notification;
import com.taskflow.notificationservice.exception.NotificationNotFoundException;
import com.taskflow.notificationservice.repository.NotificationRepository;
import com.taskflow.notificationservice.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    void shouldCreateNotification() {

        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setUserId(1L);
        request.setTitle("Task Assigned");
        request.setMessage("Complete Spring Boot Project");
        request.setType("IN_APP");

        Notification notification = Notification.builder()
                .id(1L)
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.save(any(Notification.class)))
                .thenReturn(notification);

        NotificationResponse response =
                service.createNotification(request);

        assertEquals("Task Assigned", response.getTitle());

        verify(repository, times(1))
                .save(any(Notification.class));
    }

    @Test
    void shouldReturnNotificationById() {

        Notification notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .title("Reminder")
                .message("Complete Task")
                .type("EMAIL")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                service.getNotificationById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Reminder", response.getTitle());

        verify(repository).findById(1L);
    }

    @Test
    void shouldReturnNotificationsByUser() {

        Notification notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .title("Reminder")
                .message("Task Due")
                .type("IN_APP")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findByUserId(1L))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response =
                service.getNotificationsByUser(1L);

        assertEquals(1, response.size());

        verify(repository).findByUserId(1L);
    }

    @Test
    void shouldMarkNotificationAsRead() {

        Notification notification = Notification.builder()
                .id(1L)
                .userId(1L)
                .title("Reminder")
                .message("Task Due")
                .type("IN_APP")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        when(repository.save(any(Notification.class)))
                .thenReturn(notification);

        NotificationResponse response =
                service.markAsRead(1L);

        assertTrue(response.getIsRead());

        verify(repository).save(any(Notification.class));
    }

    @Test
    void shouldDeleteNotification() {

        Notification notification = Notification.builder()
                .id(1L)
                .build();

        when(repository.findById(1L))
                .thenReturn(Optional.of(notification));

        service.deleteNotification(1L);

        verify(repository).delete(notification);
    }

    @Test
    void shouldThrowNotificationNotFoundException() {

        when(repository.findById(100L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotificationNotFoundException.class,
                () -> service.getNotificationById(100L)
        );
    }

}