package com.fintrack.repository;

import com.fintrack.entity.RecurringTransaction;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserAndIsActiveTrue(User user);
    List<RecurringTransaction> findByUser(User user);
    
    @Query("SELECT r FROM RecurringTransaction r WHERE r.isActive = true AND r.nextExecutionDate <= ?1")
    List<RecurringTransaction> findDueTransactions(LocalDate date);
}
