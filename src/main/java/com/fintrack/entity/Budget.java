package com.fintrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Budget() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getLimitAmount() { return limitAmount; }
    public void setLimitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; }

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static BudgetBuilder builder() {
        return new BudgetBuilder();
    }

    public static class BudgetBuilder {
        private User user;
        private String category;
        private BigDecimal limitAmount;
        private BigDecimal spentAmount = BigDecimal.ZERO;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean isDeleted;

        public BudgetBuilder user(User user) { this.user = user; return this; }
        public BudgetBuilder category(String category) { this.category = category; return this; }
        public BudgetBuilder limitAmount(BigDecimal limitAmount) { this.limitAmount = limitAmount; return this; }
        public BudgetBuilder spentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; return this; }
        public BudgetBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public BudgetBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public BudgetBuilder isDeleted(boolean isDeleted) { this.isDeleted = isDeleted; return this; }

        public Budget build() {
            Budget budget = new Budget();
            budget.setUser(user);
            budget.setCategory(category);
            budget.setLimitAmount(limitAmount);
            budget.setSpentAmount(spentAmount);
            budget.setStartDate(startDate);
            budget.setEndDate(endDate);
            budget.setDeleted(isDeleted);
            return budget;
        }
    }
}
