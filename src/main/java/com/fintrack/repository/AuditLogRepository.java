package com.fintrack.repository;

import com.fintrack.entity.AuditLog;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserOrderByCreatedAtDesc(User user);
}
