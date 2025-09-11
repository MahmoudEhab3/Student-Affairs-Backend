package com.unilink.service;

import com.unilink.entity.Notification;
import com.unilink.repository.NotificationRepository;
import org.springframework.stereotype.Service;
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

    public void markAsRead(Long notificationId, Integer userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new RuntimeException("Notification not found or access denied");
        }
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndStatus(userId, Notification.NotificationStatus.Unread);
    }
}