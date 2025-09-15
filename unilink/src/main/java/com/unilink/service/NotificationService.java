package com.unilink.service;

import com.unilink.entity.Notification;
import com.unilink.repository.NotificationRepository;
import com.unilink.repository.StaffRepository;
import com.unilink.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
                               StudentRepository studentRepository,
                               StaffRepository staffRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.emailService = emailService;
    }


    public Notification createNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);

        // Send email notification
        sendEmailNotification(savedNotification);

        return savedNotification;
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
    private void sendEmailNotification(Notification notification) {
        try {
            String userEmail = findUserEmail(notification.getUserId());
            if (userEmail != null) {
                String subject = "Notification: " + notification.getTitle();
                String text = buildEmailText(notification);
                emailService.sendEmail(userEmail, subject, text);
            }
        } catch (Exception e) {
            System.err.println("Failed to send email notification: " + e.getMessage());
        }
    }

    private String findUserEmail(Integer userId) {
        // Try to find student email first
        return studentRepository.findById(userId)
                .map(student -> student.getEmail())
                .orElseGet(() -> {
                    // If not a student, try to find staff email
                    return staffRepository.findById(userId)
                            .map(staff -> staff.getEmail())
                            .orElse(null);
                });
    }

    private String buildEmailText(Notification notification) {
        return String.format(
                "Dear User,\n\n" +
                        "You have a new notification:\n\n" +
                        "Title: %s\n" +
                        "Message: %s\n" +
                        "Type: %s\n" +
                        "Timestamp: %s\n\n" +
                        "Please log in to your account for more details.\n\n" +
                        "Best regards,\n" +
                        "Student Affairs System",
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getTimestamp()
        );
    }
}