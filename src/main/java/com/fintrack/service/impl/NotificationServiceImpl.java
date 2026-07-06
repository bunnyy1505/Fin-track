package com.fintrack.service.impl;

import com.fintrack.dto.NotificationResponse;
import com.fintrack.entity.Notification;
import com.fintrack.entity.User;
import com.fintrack.repository.NotificationRepository;
import com.fintrack.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public void createNotification(User user, String message, String type) {
        List<Notification> recent = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        boolean duplicate = recent.stream().anyMatch(n -> n.getMessage().equals(message));
        if (duplicate) {
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    public List<NotificationResponse> getNotifications(User user, boolean unreadOnly) {
        List<Notification> list = unreadOnly 
                ? notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user)
                : notificationRepository.findByUserOrderByCreatedAtDesc(user);

        return list.stream()
                .map(n -> modelMapper.map(n, NotificationResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(User user, Long notificationId) {
        notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .ifPresent(n -> {
                    n.setRead(true);
                    notificationRepository.save(n);
                });
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}
