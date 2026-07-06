package com.fintrack.service;

import com.fintrack.dto.BudgetRequest;
import com.fintrack.dto.BudgetResponse;
import com.fintrack.entity.User;
import java.util.List;

public interface BudgetService {
    BudgetResponse createBudget(User user, BudgetRequest budgetRequest);
    BudgetResponse getBudgetById(User user, Long id);
    List<BudgetResponse> getAllBudgets(User user);
    BudgetResponse updateBudget(User user, Long id, BudgetRequest budgetRequest);
    void deleteBudget(User user, Long id);
    void recalculateSpentAmount(User user, String category);
}
