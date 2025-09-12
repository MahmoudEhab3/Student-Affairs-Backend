package com.unilink.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private LocalDateTime timestamp;
    private boolean read;
}