package com.fintrack.repository;

import com.fintrack.entity.Budget;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserAndIsDeletedFalse(User user);

    @Query("SELECT b FROM Budget b WHERE b.user = ?1 AND b.isDeleted = false AND b.category = ?2 AND ?3 BETWEEN b.startDate AND b.endDate")
    Optional<Budget> findActiveBudgetByCategoryAndDate(User user, String category, LocalDate date);

    @Query("SELECT b FROM Budget b WHERE b.user = ?1 AND b.isDeleted = false AND ?2 BETWEEN b.startDate AND b.endDate")
    List<Budget> findActiveBudgetsByDate(User user, LocalDate date);
}
