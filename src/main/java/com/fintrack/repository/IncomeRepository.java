package com.fintrack.repository;

import com.fintrack.entity.Income;
import com.fintrack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    Page<Income> findByUserAndIsDeletedFalse(User user, Pageable pageable);
    
    @Query("SELECT i FROM Income i WHERE i.user = ?1 AND i.isDeleted = false AND i.transactionDate BETWEEN ?2 AND ?3 AND (?4 IS NULL OR LOWER(i.category) = LOWER(?4)) AND (?5 IS NULL OR LOWER(i.description) LIKE LOWER(CONCAT('%', ?5, '%')))")
    Page<Income> searchIncomes(User user, LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable);

    @Query("SELECT i FROM Income i WHERE i.user = ?1 AND i.isDeleted = false AND i.transactionDate BETWEEN ?2 AND ?3")
    List<Income> findByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user = ?1 AND i.isDeleted = false AND i.transactionDate BETWEEN ?2 AND ?3")
    BigDecimal sumIncomeByUserAndDateRange(User user, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.user = ?1 AND i.isDeleted = false AND i.transactionDate = ?2")
    BigDecimal sumIncomeByUserAndDate(User user, LocalDate date);
}
