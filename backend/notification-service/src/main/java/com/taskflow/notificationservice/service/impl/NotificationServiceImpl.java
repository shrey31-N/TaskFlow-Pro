package com.taskflow.notificationservice.service.impl;

import com.taskflow.notificationservice.dto.request.CreateNotificationRequest;
import com.taskflow.notificationservice.dto.response.NotificationResponse;
import com.taskflow.notificationservice.entity.Notification;
import com.taskflow.notificationservice.exception.NotificationNotFoundException;
import com.taskflow.notificationservice.repository.NotificationRepository;
import com.taskflow.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;

    @Override
    public NotificationResponse createNotification(CreateNotificationRequest request) {

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = repository.save(notification);

        return mapToResponse(saved);
    }

    @Override
    public NotificationResponse getNotificationById(Long id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id));
        return mapToResponse(notification);
    }

    @Override
    public List<NotificationResponse> getNotificationsByUser(Long userId) {

        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public NotificationResponse markAsRead(Long id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id));
        notification.setIsRead(true);

        Notification updated = repository.save(notification);

        return mapToResponse(updated);
    }

    @Override
    public void deleteNotification(Long id) {

        Notification notification = repository.findById(id)
                .orElseThrow(() ->
                        new NotificationNotFoundException(
                                "Notification not found with id: " + id));
        repository.delete(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}