package com.fintrack.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private String message;
    private boolean isRead;
    private String type;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
