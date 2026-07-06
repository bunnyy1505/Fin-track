package com.fintrack.service;

import com.fintrack.dto.NotificationResponse;
import com.fintrack.entity.User;
import java.util.List;

public interface NotificationService {
    void createNotification(User user, String message, String type);
    List<NotificationResponse> getNotifications(User user, boolean unreadOnly);
    void markAsRead(User user, Long notificationId);
    void markAllAsRead(User user);
}
