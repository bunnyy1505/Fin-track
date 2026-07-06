package com.fintrack.controller;

import com.fintrack.dto.DashboardSummaryResponse;
import com.fintrack.entity.Budget;
import com.fintrack.entity.Expense;
import com.fintrack.entity.Income;
import com.fintrack.entity.User;
import com.fintrack.repository.BudgetRepository;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.IncomeRepository;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboardSummary(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getAuthenticatedUser(userPrincipal);
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        BigDecimal todayIncome = incomeRepository.sumIncomeByUserAndDate(user, today);
        if (todayIncome == null) todayIncome = BigDecimal.ZERO;

        BigDecimal todayExpense = expenseRepository.sumExpenseByUserAndDate(user, today);
        if (todayExpense == null) todayExpense = BigDecimal.ZERO;

        BigDecimal monthIncome = incomeRepository.sumIncomeByUserAndDateRange(user, firstDayOfMonth, lastDayOfMonth);
        if (monthIncome == null) monthIncome = BigDecimal.ZERO;

        BigDecimal monthExpense = expenseRepository.sumExpenseByUserAndDateRange(user, firstDayOfMonth, lastDayOfMonth);
        if (monthExpense == null) monthExpense = BigDecimal.ZERO;

        BigDecimal allTimeIncome = incomeRepository.sumIncomeByUserAndDateRange(user, LocalDate.of(1970, 1, 1), LocalDate.of(2099, 12, 31));
        if (allTimeIncome == null) allTimeIncome = BigDecimal.ZERO;

        BigDecimal allTimeExpense = expenseRepository.sumExpenseByUserAndDateRange(user, LocalDate.of(1970, 1, 1), LocalDate.of(2099, 12, 31));
        if (allTimeExpense == null) allTimeExpense = BigDecimal.ZERO;

        BigDecimal currentBalance = allTimeIncome.subtract(allTimeExpense);
        BigDecimal monthlySavings = monthIncome.subtract(monthExpense);

        List<Income> rawIncomes = incomeRepository.findByUserAndTransactionDateBetween(user, today.minusDays(30), today);
        List<Expense> rawExpenses = expenseRepository.findByUserAndTransactionDateBetween(user, today.minusDays(30), today);

        List<DashboardSummaryResponse.TransactionDto> combinedTxns = new ArrayList<>();
        for (Income inc : rawIncomes) {
            combinedTxns.add(DashboardSummaryResponse.TransactionDto.builder()
                    .id(inc.getId())
                    .type("INCOME")
                    .amount(inc.getAmount())
                    .category(inc.getCategory())
                    .description(inc.getDescription())
                    .date(inc.getTransactionDate().toString())
                    .build());
        }

        for (Expense exp : rawExpenses) {
            combinedTxns.add(DashboardSummaryResponse.TransactionDto.builder()
                    .id(exp.getId())
                    .type("EXPENSE")
                    .amount(exp.getAmount())
                    .category(exp.getCategory())
                    .description(exp.getDescription())
                    .date(exp.getTransactionDate().toString())
                    .build());
        }

        combinedTxns.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        List<DashboardSummaryResponse.TransactionDto> latestTxns = combinedTxns.stream().limit(5).collect(Collectors.toList());

        List<Budget> budgets = budgetRepository.findActiveBudgetsByDate(user, today);
        List<DashboardSummaryResponse.BudgetProgressDto> budgetProgress = budgets.stream().map(b -> {
            BigDecimal limit = b.getLimitAmount();
            BigDecimal spent = b.getSpentAmount();
            BigDecimal remaining = limit.subtract(spent);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

            double utilPct = 0;
            if (limit.compareTo(BigDecimal.ZERO) > 0) {
                utilPct = spent.multiply(new BigDecimal(100)).divide(limit, 2, RoundingMode.HALF_UP).doubleValue();
            }

            return DashboardSummaryResponse.BudgetProgressDto.builder()
                    .id(b.getId())
                    .category(b.getCategory())
                    .limitAmount(limit)
                    .spentAmount(spent)
                    .remainingAmount(remaining)
                    .utilizationPercentage(utilPct)
                    .isExceeded(spent.compareTo(limit) > 0)
                    .build();
        }).collect(Collectors.toList());

        int healthScore = 0;
        if (monthIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingRatio = monthlySavings.divide(monthIncome, 4, RoundingMode.HALF_UP);
            if (savingRatio.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal scoreBase = savingRatio.multiply(new BigDecimal(200));
                healthScore = scoreBase.setScale(0, RoundingMode.HALF_UP).intValue();
                if (healthScore > 100) healthScore = 100;
                if (healthScore < 0) healthScore = 0;
            }
        } else if (monthExpense.compareTo(BigDecimal.ZERO) == 0) {
            healthScore = 100;
        }

        DashboardSummaryResponse summary = DashboardSummaryResponse.builder()
                .totalIncome(monthIncome)
                .totalExpense(monthExpense)
                .currentBalance(currentBalance)
                .monthlySavings(monthlySavings)
                .todayIncome(todayIncome)
                .todayExpense(todayExpense)
                .latestTransactions(latestTxns)
                .budgetProgress(budgetProgress)
                .financialHealthScore(healthScore)
                .build();

        return ResponseEntity.ok(new ApiResponse<>(200, "Dashboard summary calculated", summary));
    }
}
