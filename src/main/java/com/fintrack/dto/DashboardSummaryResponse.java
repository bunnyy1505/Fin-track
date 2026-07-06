package com.fintrack.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardSummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal currentBalance;
    private BigDecimal monthlySavings;
    private BigDecimal todayExpense;
    private BigDecimal todayIncome;
    private List<TransactionDto> latestTransactions;
    private List<BudgetProgressDto> budgetProgress;
    private int financialHealthScore;

    public DashboardSummaryResponse() {}

    public DashboardSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal currentBalance, BigDecimal monthlySavings, BigDecimal todayExpense, BigDecimal todayIncome, List<TransactionDto> latestTransactions, List<BudgetProgressDto> budgetProgress, int financialHealthScore) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.currentBalance = currentBalance;
        this.monthlySavings = monthlySavings;
        this.todayExpense = todayExpense;
        this.todayIncome = todayIncome;
        this.latestTransactions = latestTransactions;
        this.budgetProgress = budgetProgress;
        this.financialHealthScore = financialHealthScore;
    }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }

    public BigDecimal getMonthlySavings() { return monthlySavings; }
    public void setMonthlySavings(BigDecimal monthlySavings) { this.monthlySavings = monthlySavings; }

    public BigDecimal getTodayExpense() { return todayExpense; }
    public void setTodayExpense(BigDecimal todayExpense) { this.todayExpense = todayExpense; }

    public BigDecimal getTodayIncome() { return todayIncome; }
    public void setTodayIncome(BigDecimal todayIncome) { this.todayIncome = todayIncome; }

    public List<TransactionDto> getLatestTransactions() { return latestTransactions; }
    public void setLatestTransactions(List<TransactionDto> latestTransactions) { this.latestTransactions = latestTransactions; }

    public List<BudgetProgressDto> getBudgetProgress() { return budgetProgress; }
    public void setBudgetProgress(List<BudgetProgressDto> budgetProgress) { this.budgetProgress = budgetProgress; }

    public int getFinancialHealthScore() { return financialHealthScore; }
    public void setFinancialHealthScore(int financialHealthScore) { this.financialHealthScore = financialHealthScore; }

    public static DashboardSummaryResponseBuilder builder() {
        return new DashboardSummaryResponseBuilder();
    }

    public static class DashboardSummaryResponseBuilder {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal currentBalance;
        private BigDecimal monthlySavings;
        private BigDecimal todayExpense;
        private BigDecimal todayIncome;
        private List<TransactionDto> latestTransactions;
        private List<BudgetProgressDto> budgetProgress;
        private int financialHealthScore;

        public DashboardSummaryResponseBuilder totalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; return this; }
        public DashboardSummaryResponseBuilder totalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; return this; }
        public DashboardSummaryResponseBuilder currentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; return this; }
        public DashboardSummaryResponseBuilder monthlySavings(BigDecimal monthlySavings) { this.monthlySavings = monthlySavings; return this; }
        public DashboardSummaryResponseBuilder todayExpense(BigDecimal todayExpense) { this.todayExpense = todayExpense; return this; }
        public DashboardSummaryResponseBuilder todayIncome(BigDecimal todayIncome) { this.todayIncome = todayIncome; return this; }
        public DashboardSummaryResponseBuilder latestTransactions(List<TransactionDto> latestTransactions) { this.latestTransactions = latestTransactions; return this; }
        public DashboardSummaryResponseBuilder budgetProgress(List<BudgetProgressDto> budgetProgress) { this.budgetProgress = budgetProgress; return this; }
        public DashboardSummaryResponseBuilder financialHealthScore(int financialHealthScore) { this.financialHealthScore = financialHealthScore; return this; }

        public DashboardSummaryResponse build() {
            return new DashboardSummaryResponse(totalIncome, totalExpense, currentBalance, monthlySavings, todayExpense, todayIncome, latestTransactions, budgetProgress, financialHealthScore);
        }
    }

    public static class TransactionDto {
        private Long id;
        private String type;
        private BigDecimal amount;
        private String category;
        private String description;
        private String date;

        public TransactionDto() {}

        public TransactionDto(Long id, String type, BigDecimal amount, String category, String description, String date) {
            this.id = id;
            this.type = type;
            this.amount = amount;
            this.category = category;
            this.description = description;
            this.date = date;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public static TransactionDtoBuilder builder() {
            return new TransactionDtoBuilder();
        }

        public static class TransactionDtoBuilder {
            private Long id;
            private String type;
            private BigDecimal amount;
            private String category;
            private String description;
            private String date;

            public TransactionDtoBuilder id(Long id) { this.id = id; return this; }
            public TransactionDtoBuilder type(String type) { this.type = type; return this; }
            public TransactionDtoBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
            public TransactionDtoBuilder category(String category) { this.category = category; return this; }
            public TransactionDtoBuilder description(String description) { this.description = description; return this; }
            public TransactionDtoBuilder date(String date) { this.date = date; return this; }

            public TransactionDto build() {
                return new TransactionDto(id, type, amount, category, description, date);
            }
        }
    }

    public static class BudgetProgressDto {
        private Long id;
        private String category;
        private BigDecimal limitAmount;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private double utilizationPercentage;
        private boolean isExceeded;

        public BudgetProgressDto() {}

        public BudgetProgressDto(Long id, String category, BigDecimal limitAmount, BigDecimal spentAmount, BigDecimal remainingAmount, double utilizationPercentage, boolean isExceeded) {
            this.id = id;
            this.category = category;
            this.limitAmount = limitAmount;
            this.spentAmount = spentAmount;
            this.remainingAmount = remainingAmount;
            this.utilizationPercentage = utilizationPercentage;
            this.isExceeded = isExceeded;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public BigDecimal getLimitAmount() { return limitAmount; }
        public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

        public BigDecimal getSpentAmount() { return spentAmount; }
        public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

        public double getUtilizationPercentage() { return utilizationPercentage; }
        public void setUtilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; }

        public boolean isExceeded() { return isExceeded; }
        public void setExceeded(boolean exceeded) { isExceeded = exceeded; }

        public static BudgetProgressDtoBuilder builder() {
            return new BudgetProgressDtoBuilder();
        }

        public static class BudgetProgressDtoBuilder {
            private Long id;
            private String category;
            private BigDecimal limitAmount;
            private BigDecimal spentAmount;
            private BigDecimal remainingAmount;
            private double utilizationPercentage;
            private boolean isExceeded;

            public BudgetProgressDtoBuilder id(Long id) { this.id = id; return this; }
            public BudgetProgressDtoBuilder category(String category) { this.category = category; return this; }
            public BudgetProgressDtoBuilder limitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; return this; }
            public BudgetProgressDtoBuilder spentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; return this; }
            public BudgetProgressDtoBuilder remainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; return this; }
            public BudgetProgressDtoBuilder utilizationPercentage(double utilizationPercentage) { this.utilizationPercentage = utilizationPercentage; return this; }
            public BudgetProgressDtoBuilder isExceeded(boolean isExceeded) { this.isExceeded = isExceeded; return this; }

            public BudgetProgressDto build() {
                return new BudgetProgressDto(id, category, limitAmount, spentAmount, remainingAmount, utilizationPercentage, isExceeded);
            }
        }
    }
}
