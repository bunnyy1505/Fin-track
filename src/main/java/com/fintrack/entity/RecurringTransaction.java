package com.fintrack.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
public class RecurringTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 15)
    private String frequency;

    private LocalDate lastExecutionDate;

    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    @Column(nullable = false)
    private boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecurringTransaction() {}

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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getLastExecutionDate() { return lastExecutionDate; }
    public void setLastExecutionDate(LocalDate lastExecutionDate) { this.lastExecutionDate = lastExecutionDate; }

    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public void setNextExecutionDate(LocalDate nextExecutionDate) { this.nextExecutionDate = nextExecutionDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static RecurringTransactionBuilder builder() {
        return new RecurringTransactionBuilder();
    }

    public static class RecurringTransactionBuilder {
        private User user;
        private String type;
        private BigDecimal amount;
        private String category;
        private String description;
        private String frequency;
        private LocalDate lastExecutionDate;
        private LocalDate nextExecutionDate;
        private boolean isActive = true;

        public RecurringTransactionBuilder user(User user) { this.user = user; return this; }
        public RecurringTransactionBuilder type(String type) { this.type = type; return this; }
        public RecurringTransactionBuilder amount(BigDecimal amount) { this.amount = amount; return this; }
        public RecurringTransactionBuilder category(String category) { this.category = category; return this; }
        public RecurringTransactionBuilder description(String description) { this.description = description; return this; }
        public RecurringTransactionBuilder frequency(String frequency) { this.frequency = frequency; return this; }
        public RecurringTransactionBuilder lastExecutionDate(LocalDate lastExecutionDate) { this.lastExecutionDate = lastExecutionDate; return this; }
        public RecurringTransactionBuilder nextExecutionDate(LocalDate nextExecutionDate) { this.nextExecutionDate = nextExecutionDate; return this; }
        public RecurringTransactionBuilder isActive(boolean isActive) { this.isActive = isActive; return this; }

        public RecurringTransaction build() {
            RecurringTransaction rt = new RecurringTransaction();
            rt.setUser(user);
            rt.setType(type);
            rt.setAmount(amount);
            rt.setCategory(category);
            rt.setDescription(description);
            rt.setFrequency(frequency);
            rt.setLastExecutionDate(lastExecutionDate);
            rt.setNextExecutionDate(nextExecutionDate);
            rt.setActive(isActive);
            return rt;
        }
    }
}
