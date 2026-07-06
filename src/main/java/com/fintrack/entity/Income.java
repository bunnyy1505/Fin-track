package com.fintrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "income")
public class Income {
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

    public Income() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    // Builder pattern fallback
    public static IncomeBuilder builder() {
        return new IncomeBuilder();
    }

    public static class IncomeBuilder {
        private User user;
        private BigDecimal amount;
        private String category;
        private String description;
        private LocalDate transactionDate;
        private boolean isDeleted;

        public IncomeBuilder user(User user) { this.user = user; return this; }
        public IncomeBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public IncomeBuilder category(String category) { this.category = category; return this; }
        public IncomeBuilder description(String description) { this.description = description; return this; }
        public IncomeBuilder transactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; return this; }
        public IncomeBuilder isDeleted(boolean isDeleted) { this.isDeleted = isDeleted; return this; }

        public Income build() {
            Income inc = new Income();
            inc.setUser(user);
            inc.setAmount(amount);
            inc.setCategory(category);
            inc.setDescription(description);
            inc.setTransactionDate(transactionDate);
            inc.setDeleted(isDeleted);
            return inc;
        }
    }
}
