package com.unilink.service;

import com.unilink.entity.Notification;
import com.unilink.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Integer userId, Long lastNotificationId) {
        if (lastNotificationId != null && lastNotificationId > 0) {
            return notificationRepository.findNewNotifications(userId, lastNotificationId);
        }
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Integer userId) {
        // First, verify the notification exists and belongs to the user
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: Notification does not belong to user");
        }

        // Update the status
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new RuntimeException("Failed to mark notification as read");
        }
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
}