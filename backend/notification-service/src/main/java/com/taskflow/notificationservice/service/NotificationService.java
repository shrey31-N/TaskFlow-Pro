package com.taskflow.notificationservice.service;

import com.taskflow.notificationservice.dto.request.CreateNotificationRequest;
import com.taskflow.notificationservice.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(CreateNotificationRequest request);

    NotificationResponse getNotificationById(Long id);

    List<NotificationResponse> getNotificationsByUser(Long userId);

    NotificationResponse markAsRead(Long id);

    void deleteNotification(Long id);

}