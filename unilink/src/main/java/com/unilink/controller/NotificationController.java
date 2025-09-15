package com.unilink.controller;

import com.unilink.config.JwtUtil;
import com.unilink.dto.NotificationDTO;
import com.unilink.entity.Notification;
import com.unilink.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public NotificationController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Integer getUserIdFromToken(String token) {
        String role = jwtUtil.getRoleFromToken(token);
        if ("STUDENT".equals(role)) {
            return jwtUtil.getStudentIdFromToken(token);
        } else {
            return jwtUtil.getStaffIdFromToken(token);
        }
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(HttpServletRequest request,
                                              @RequestParam(required = false) Long lastNotificationId) {
        String token = getTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        try {
            Integer userId = getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID not found in token");
            }

            List<Notification> notifications = notificationService.getUserNotifications(userId, lastNotificationId);
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(notificationDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to retrieve notifications: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        try {
            Integer userId = getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID not found in token");
            }

            notificationService.markAsRead(id, userId);
            return ResponseEntity.ok().body("Notification marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to mark notification as read: " + e.getMessage());
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        try {
            Integer userId = getUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.badRequest().body("User ID not found in token");
            }

            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok().body(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get unread count: " + e.getMessage());
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType().name());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setTimestamp(notification.getTimestamp());
        dto.setRead(notification.isRead());
        return dto;
    }

    // Add this method to your existing NotificationController
    @PostMapping("/create")
    public ResponseEntity<?> createNotification(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam String type,
            HttpServletRequest request) {

        String token = getTokenFromRequest(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return ResponseEntity.badRequest().body("Invalid token");
        }

        Integer userId = getUserIdFromToken(token);
        if (userId == null) {
            return ResponseEntity.badRequest().body("User ID not found in token");
        }

        // Create a new Notification object
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(Notification.NotificationType.valueOf(type));
        notification.setTimestamp(LocalDateTime.now());
        notification.setStatus(Notification.NotificationStatus.valueOf("Unread"));

        try {
            Notification createdNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(createdNotification);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create notification: " + e.getMessage());
        }
    }
}