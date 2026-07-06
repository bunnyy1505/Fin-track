package com.fintrack.repository;

import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findByUserAndIsDeletedFalse(User user, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user = ?1 AND e.isDeleted = false AND e.transactionDate BETWEEN ?2 AND ?3 AND (?4 IS NULL OR LOWER(e.category) = LOWER(?4)) AND (?5 IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', ?5, '%')))")
    Page<Expense> searchExpenses(User user, LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.user = ?1 AND e.isDeleted = false AND e.transactionDate BETWEEN ?2 AND ?3")
    List<Expense> findByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = ?1 AND e.isDeleted = false AND e.transactionDate BETWEEN ?2 AND ?3")
    BigDecimal sumExpenseByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = ?1 AND e.isDeleted = false AND e.transactionDate BETWEEN ?2 AND ?3 AND LOWER(e.category) = LOWER(?4)")
    BigDecimal sumExpenseByUserAndDateRangeAndCategory(User user, LocalDate startDate, LocalDate endDate, String category);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user = ?1 AND e.isDeleted = false AND e.transactionDate = ?2")
    BigDecimal sumExpenseByUserAndDate(User user, LocalDate date);
}
