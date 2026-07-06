package com.fintrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RecurringTransactionResponse {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String category;
    private String description;
    private String frequency;
    private LocalDate lastExecutionDate;
    private LocalDate nextExecutionDate;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RecurringTransactionResponse() {}

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

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public LocalDate getLastExecutionDate() { return lastExecutionDate; }
    public void setLastExecutionDate(LocalDate lastExecutionDate) { this.lastExecutionDate = lastExecutionDate; }

    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public void setNextExecutionDate(LocalDate nextExecutionDate) { this.nextExecutionDate = nextExecutionDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
