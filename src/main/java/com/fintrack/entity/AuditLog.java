package com.fintrack.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 1000)
    private String details;

    @Column(length = 45)
    private String ipAddress;

    private LocalDateTime createdAt;

    public AuditLog() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private User user;
        private String action;
        private String details;
        private String ipAddress;

        public AuditLogBuilder user(User user) { this.user = user; return this; }
        public AuditLogBuilder action(String action) { this.action = action; return this; }
        public AuditLogBuilder details(String details) { this.details = details; return this; }
        public AuditLogBuilder ipAddress(String ipAddress) { this.ipAddress = ipAddress; return this; }

        public AuditLog build() {
            AuditLog auditLog = new AuditLog();
            auditLog.setUser(user);
            auditLog.setAction(action);
            auditLog.setDetails(details);
            auditLog.setIpAddress(ipAddress);
            return auditLog;
        }
    }
}
