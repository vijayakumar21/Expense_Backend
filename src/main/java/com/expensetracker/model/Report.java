package com.expensetracker.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Report {
    // stored properties. null id means the report is not saved yet
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    // for precision involving money;
    private BigDecimal budget;

    // computed properties, not stored ones. Calc based on stored properties.
    // Only totalSpent and expenseCount are set from outside; the rest are derived.
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal remaining;
    private Double budgetUsedPercent;
    private String status = "NO_BUDGET";
    private Integer expenseCount = 0;

    private List<Expense> expenses;

    public Report() {
    }

    public Long getId() { return id; }
    public void setId(Long id) {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("id must be greater than 0");
        }
        this.id = id;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        this.userId = userId;
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        String trimmed = name.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("name must be at most 100 characters");
        }
        this.name = trimmed;
    }

    // Optional: blank description is stored as null
    public String getDescription() { return description; }
    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            this.description = null;
            return;
        }
        String trimmed = description.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("description must be at most 500 characters");
        }
        this.description = trimmed;
    }

    // Optional. The date order is checked in both setters so the call order does not matter.
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("startDate must not be after the end date");
        }
        this.startDate = startDate;
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) {
        if (endDate != null && startDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate must not be before the start date");
        }
        this.endDate = endDate;
    }

    // Optional: null means no budget
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) {
        if (budget != null && budget.signum() < 0) {
            throw new IllegalArgumentException("budget must be 0 or more");
        }
        this.budget = budget;
        computeSummary();
    }

    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent == null ? BigDecimal.ZERO : totalSpent;
        computeSummary();
    }

    public Integer getExpenseCount() { return expenseCount; }
    public void setExpenseCount(Integer expenseCount) {
        if (expenseCount != null && expenseCount < 0) {
            throw new IllegalArgumentException("expenseCount must not be negative");
        }
        this.expenseCount = expenseCount == null ? 0 : expenseCount;
    }

    // read-only: derived from budget and totalSpent
    public BigDecimal getRemaining() { return remaining; }
    public Double getBudgetUsedPercent() { return budgetUsedPercent; }
    public String getStatus() { return status; }

    // read-only view, so callers cannot change the list behind the report's back
    public List<Expense> getExpenses() {
        return expenses == null ? null : List.copyOf(expenses);
    }
    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses == null ? null : new ArrayList<>(expenses);
    }

    private void computeSummary() {
        if (budget == null) {
            remaining = null;
            budgetUsedPercent = null;
            status = "NO_BUDGET";
            return;
        }

        remaining = budget.subtract(totalSpent);

        if (budget.signum() == 0) {
            budgetUsedPercent = totalSpent.signum() > 0 ? 100.0 : 0.0;
        } else {
            budgetUsedPercent = totalSpent.multiply(BigDecimal.valueOf(100))
                    .divide(budget, 1, RoundingMode.HALF_UP).doubleValue();
        }

        if (remaining.signum() < 0) {
            status = "OVER";
        } else if (budgetUsedPercent >= 75.0) {
            status = "WARN";
        } else {
            status = "OK";
        }
    }

}
