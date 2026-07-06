package com.fintrack.service;

import com.fintrack.dto.ExpenseRequest;
import com.fintrack.dto.ExpenseResponse;
import com.fintrack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(User user, ExpenseRequest expenseRequest);
    ExpenseResponse getExpenseById(User user, Long id);
    Page<ExpenseResponse> getAllExpenses(User user, Pageable pageable);
    Page<ExpenseResponse> searchExpenses(User user, LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable);
    ExpenseResponse updateExpense(User user, Long id, ExpenseRequest expenseRequest);
    void deleteExpense(User user, Long id);
    List<ExpenseResponse> getExpensesForPeriod(User user, LocalDate startDate, LocalDate endDate);
}
