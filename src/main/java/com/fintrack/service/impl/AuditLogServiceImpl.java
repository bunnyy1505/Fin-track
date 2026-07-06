package com.fintrack.service.impl;

import com.fintrack.entity.AuditLog;
import com.fintrack.entity.User;
import com.fintrack.repository.AuditLogRepository;
import com.fintrack.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void logAction(User user, String action, String details, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
    }
}
