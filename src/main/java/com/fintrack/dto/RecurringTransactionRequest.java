package com.fintrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringTransactionRequest {
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(INCOME|EXPENSE)$", message = "Type must be either INCOME or EXPENSE")
    private String type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "^(DAILY|WEEKLY|MONTHLY)$", message = "Frequency must be DAILY, WEEKLY, or MONTHLY")
    private String frequency;

    @NotNull(message = "Next execution date is required")
    private LocalDate nextExecutionDate;

    public RecurringTransactionRequest() {}

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

    public LocalDate getNextExecutionDate() { return nextExecutionDate; }
    public void setNextExecutionDate(LocalDate nextExecutionDate) { this.nextExecutionDate = nextExecutionDate; }
}
