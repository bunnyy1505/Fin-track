package com.fintrack.service.impl;

import com.fintrack.dto.BudgetRequest;
import com.fintrack.dto.BudgetResponse;
import com.fintrack.entity.Budget;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.BudgetRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.service.AuditLogService;
import com.fintrack.service.BudgetService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public BudgetResponse createBudget(User user, BudgetRequest request) {
        Budget budget = modelMapper.map(request, Budget.class);
        budget.setUser(user);
        budget.setDeleted(false);

        BigDecimal totalSpent;
        if (budget.getCategory().equalsIgnoreCase("ALL")) {
            totalSpent = expenseRepository.sumExpenseByUserAndDateRange(user, budget.getStartDate(), budget.getEndDate());
        } else {
            totalSpent = expenseRepository.sumExpenseByUserAndDateRangeAndCategory(user, budget.getStartDate(), budget.getEndDate(), budget.getCategory());
        }
        budget.setSpentAmount(totalSpent != null ? totalSpent : BigDecimal.ZERO);

        Budget saved = budgetRepository.save(budget);

        auditLogService.logAction(user, "CREATE_BUDGET", "Configured budget for " + saved.getCategory() + " with limit " + saved.getLimitAmount(), "0.0.0.0");

        return modelMapper.map(saved, BudgetResponse.class);
    }

    @Override
    public BudgetResponse getBudgetById(User user, Long id) {
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(user.getId()) && !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id " + id));
        return modelMapper.map(budget, BudgetResponse.class);
    }

    @Override
    public List<BudgetResponse> getAllBudgets(User user) {
        return budgetRepository.findByUserAndIsDeletedFalse(user).stream()
                .map(b -> modelMapper.map(b, BudgetResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(User user, Long id, BudgetRequest request) {
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(user.getId()) && !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id " + id));

        budget.setCategory(request.getCategory());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());

        BigDecimal totalSpent;
        if (budget.getCategory().equalsIgnoreCase("ALL")) {
            totalSpent = expenseRepository.sumExpenseByUserAndDateRange(user, budget.getStartDate(), budget.getEndDate());
        } else {
            totalSpent = expenseRepository.sumExpenseByUserAndDateRangeAndCategory(user, budget.getStartDate(), budget.getEndDate(), budget.getCategory());
        }
        budget.setSpentAmount(totalSpent != null ? totalSpent : BigDecimal.ZERO);

        Budget updated = budgetRepository.save(budget);

        auditLogService.logAction(user, "UPDATE_BUDGET", "Modified budget: ID=" + id + " limit=" + updated.getLimitAmount(), "0.0.0.0");

        return modelMapper.map(updated, BudgetResponse.class);
    }

    @Override
    @Transactional
    public void deleteBudget(User user, Long id) {
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(user.getId()) && !b.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found with id " + id));

        budget.setDeleted(true);
        budgetRepository.save(budget);

        auditLogService.logAction(user, "DELETE_BUDGET", "Deleted budget: ID=" + id, "0.0.0.0");
    }

    @Override
    @Transactional
    public void recalculateSpentAmount(User user, String category) {
    }
}
