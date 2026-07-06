package com.fintrack.service;

import com.fintrack.entity.User;

public interface AuditLogService {
    void logAction(User user, String action, String details, String ipAddress);
}
