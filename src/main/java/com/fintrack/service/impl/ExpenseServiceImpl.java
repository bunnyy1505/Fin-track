package com.fintrack.service.impl;

import com.fintrack.dto.ExpenseRequest;
import com.fintrack.dto.ExpenseResponse;
import com.fintrack.entity.Budget;
import com.fintrack.entity.Expense;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.BudgetRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.service.AuditLogService;
import com.fintrack.service.EmailService;
import com.fintrack.service.ExpenseService;
import com.fintrack.service.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public ExpenseResponse createExpense(User user, ExpenseRequest request) {
        Expense expense = modelMapper.map(request, Expense.class);
        expense.setUser(user);
        expense.setDeleted(false);
        Expense saved = expenseRepository.save(expense);

        auditLogService.logAction(user, "CREATE_EXPENSE", "Added expense: " + saved.getAmount() + " in " + saved.getCategory(), "0.0.0.0");

        updateBudgetUtilization(user, saved.getCategory(), saved.getTransactionDate());
        updateBudgetUtilization(user, "ALL", saved.getTransactionDate());

        return modelMapper.map(saved, ExpenseResponse.class);
    }

    @Override
    public ExpenseResponse getExpenseById(User user, Long id) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()) && !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));
        return modelMapper.map(expense, ExpenseResponse.class);
    }

    @Override
    public Page<ExpenseResponse> getAllExpenses(User user, Pageable pageable) {
        Page<Expense> expenses = expenseRepository.findByUserAndIsDeletedFalse(user, pageable);
        return expenses.map(e -> modelMapper.map(e, ExpenseResponse.class));
    }

    @Override
    public Page<ExpenseResponse> searchExpenses(User user, LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable) {
        if (startDate == null) startDate = LocalDate.of(1970, 1, 1);
        if (endDate == null) endDate = LocalDate.of(2099, 12, 31);

        String cat = (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("all")) ? null : category;
        String query = (search == null || search.trim().isEmpty()) ? null : search;

        Page<Expense> expenses = expenseRepository.searchExpenses(user, startDate, endDate, cat, query, pageable);
        return expenses.map(e -> modelMapper.map(e, ExpenseResponse.class));
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(User user, Long id, ExpenseRequest request) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()) && !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));

        String oldCategory = expense.getCategory();
        LocalDate oldDate = expense.getTransactionDate();

        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setTransactionDate(request.getTransactionDate());
        Expense updated = expenseRepository.save(expense);

        auditLogService.logAction(user, "UPDATE_EXPENSE", "Modified expense: ID=" + id + " amount=" + updated.getAmount(), "0.0.0.0");

        updateBudgetUtilization(user, oldCategory, oldDate);
        updateBudgetUtilization(user, updated.getCategory(), updated.getTransactionDate());
        updateBudgetUtilization(user, "ALL", oldDate);
        updateBudgetUtilization(user, "ALL", updated.getTransactionDate());

        return modelMapper.map(updated, ExpenseResponse.class);
    }

    @Override
    @Transactional
    public void deleteExpense(User user, Long id) {
        Expense expense = expenseRepository.findById(id)
                .filter(e -> e.getUser().getId().equals(user.getId()) && !e.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id " + id));

        expense.setDeleted(true);
        Expense saved = expenseRepository.save(expense);

        auditLogService.logAction(user, "DELETE_EXPENSE", "Deleted expense: ID=" + id, "0.0.0.0");

        updateBudgetUtilization(user, saved.getCategory(), saved.getTransactionDate());
        updateBudgetUtilization(user, "ALL", saved.getTransactionDate());
    }

    @Override
    public List<ExpenseResponse> getExpensesForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserAndTransactionDateBetween(user, startDate, endDate).stream()
                .map(e -> modelMapper.map(e, ExpenseResponse.class))
                .collect(Collectors.toList());
    }

    private void updateBudgetUtilization(User user, String category, LocalDate date) {
        Optional<Budget> activeBudgetOpt = budgetRepository.findActiveBudgetByCategoryAndDate(user, category, date);
        if (activeBudgetOpt.isPresent()) {
            Budget budget = activeBudgetOpt.get();
            BigDecimal totalSpent;
            if (category.equalsIgnoreCase("ALL")) {
                totalSpent = expenseRepository.sumExpenseByUserAndDateRange(user, budget.getStartDate(), budget.getEndDate());
            } else {
                totalSpent = expenseRepository.sumExpenseByUserAndDateRangeAndCategory(user, budget.getStartDate(), budget.getEndDate(), category);
            }
            if (totalSpent == null) {
                totalSpent = BigDecimal.ZERO;
            }
            budget.setSpentAmount(totalSpent);
            budgetRepository.save(budget);

            if (totalSpent.compareTo(budget.getLimitAmount()) > 0) {
                String message = String.format("Alert: Your budget for category '%s' has been exceeded! Limit: %s, Spent: %s", 
                        category, budget.getLimitAmount(), totalSpent);
                notificationService.createNotification(user, message, "BUDGET_EXCEEDED");
                sendBudgetAlertEmail(user, budget, totalSpent, category);
            }
        }
    }

    private void sendBudgetAlertEmail(User user, Budget budget, BigDecimal totalSpent, String category) {
        String subject = "FinTrack - Budget Alert: Limit Exceeded";
        String body = String.format("Hello %s,\n\n" +
                "This is an automated alert from your FinTrack Personal Finance Dashboard.\n\n" +
                "You have exceeded your budget limit for category: %s\n" +
                "Budget Limit: $%s\n" +
                "Current Spending: $%s\n" +
                "Period: %s to %s\n\n" +
                "Please review your expenses to stay on track with your financial goals!\n\n" +
                "Best regards,\n" +
                "The FinTrack Team", 
                user.getFullName(), category, budget.getLimitAmount(), totalSpent, budget.getStartDate(), budget.getEndDate());
        emailService.sendEmail(user.getEmail(), subject, body);
    }
}
