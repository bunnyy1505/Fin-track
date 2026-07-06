package com.fintrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Expense() {}

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

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ExpenseBuilder builder() {
        return new ExpenseBuilder();
    }

    public static class ExpenseBuilder {
        private User user;
        private BigDecimal amount;
        private String category;
        private String description;
        private LocalDate transactionDate;
        private boolean isDeleted;

        public ExpenseBuilder user(User user) { this.user = user; return this; }
        public ExpenseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public ExpenseBuilder category(String category) { this.category = category; return this; }
        public ExpenseBuilder description(String description) { this.description = description; return this; }
        public ExpenseBuilder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public ExpenseBuilder isDeleted(boolean isDeleted) { this.isDeleted = isDeleted; return this; }

        public Expense build() {
            Expense exp = new Expense();
            exp.setUser(user);
            exp.setAmount(amount);
            exp.setCategory(category);
            exp.setDescription(description);
            exp.setTransactionDate(transactionDate);
            exp.setDeleted(isDeleted);
            return exp;
        }
    }
}
