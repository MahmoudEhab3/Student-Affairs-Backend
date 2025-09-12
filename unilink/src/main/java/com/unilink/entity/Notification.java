package com.unilink.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotifID")
    private Long id;

    @Column(name = "UserID", nullable = false)
    private Integer userId;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(name = "Type", nullable = false)
    private NotificationType type;

    @Column(name = "Title", nullable = false, length = 255)
    private String title;

    @Column(name = "Message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp", insertable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", columnDefinition = "ENUM('Unread', 'Read') DEFAULT 'Unread'")
    private NotificationStatus status;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = NotificationStatus.Unread;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return status == NotificationStatus.Read;
    }

    public enum NotificationType {
        APPOINTMENT, REQUEST, SYSTEM, ALERT
    }

    public enum NotificationStatus {
        Unread, Read
    }

    // Converter class to handle case sensitivity
    @Converter
    public static class NotificationTypeConverter implements AttributeConverter<NotificationType, String> {

        @Override
        public String convertToDatabaseColumn(NotificationType attribute) {
            if (attribute == null) {
                return null;
            }
            return attribute.name().toLowerCase(); // Store as lowercase in DB
        }

        @Override
        public NotificationType convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return null;
            }
            try {
                return NotificationType.valueOf(dbData.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Handle unknown values gracefully
                return NotificationType.SYSTEM; // Default value
            }
        }
    }
}