package com.expensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Expense {
    private long id;
    private Long userId;
    private Long reportId;
    private Long categoryId;
    private String title;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String notes;
    private String reportName;
    private String categoryName;

    public Expense() {

    }

    public Expense(long id, Long reportId, Long categoryId, String title, BigDecimal amount, LocalDate expenseDate,
            String notes, String reportName, String categoryName) {
        setId(id);
        setReportId(reportId);
        setCategoryId(categoryId);
        setTitle(title);
        setAmount(amount);
        setExpenseDate(expenseDate);
        setNotes(notes);
        setReportName(reportName);
        setCategoryName(categoryName);
    }

    public long getId() { return this.id; }
    public void setId(long id) {
        if (id < 0) { throw new IllegalArgumentException("id must not be negative"); }
        this.id = id;
    }

    public Long getUserId() { return this.userId; }
    public void setUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId must be greater than 0");
        }
        this.userId = userId;
    }

    // Optional: null means the expense is not linked to a report
    public Long getReportId() { return this.reportId; }
    public void setReportId(Long reportId) {
        if (reportId != null && reportId <= 0) {
            throw new IllegalArgumentException("reportId must be greater than 0");
        }
        this.reportId = reportId;
    }

    // Optional: null means the expense has no category
    public Long getCategoryId() { return this.categoryId; }
    public void setCategoryId(Long categoryId) {
        if (categoryId != null && categoryId <= 0) {
            throw new IllegalArgumentException("categoryId must be greater than 0");
        }
        this.categoryId = categoryId;
    }

    public String getTitle() { return this.title; }
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        String trimmed = title.trim();
        if (trimmed.length() > 100) {
            throw new IllegalArgumentException("title must be at most 100 characters");
        }
        this.title = trimmed;
    }

    public BigDecimal getAmount() { return this.amount; }
    public void setAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        this.amount = amount;
    }

    public LocalDate getExpenseDate() { return this.expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) {
        if (expenseDate == null) {
            throw new IllegalArgumentException("expenseDate is required");
        }
        this.expenseDate = expenseDate;
    }

    // Optional: blank notes are stored as null
    public String getNotes() { return this.notes; }
    public void setNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            this.notes = null;
            return;
        }
        String trimmed = notes.trim();
        if (trimmed.length() > 500) {
            throw new IllegalArgumentException("notes must be at most 500 characters");
        }
        this.notes = trimmed;
    }

    // Read-only values joined in from other tables, no rules apply
    public String getReportName() { return this.reportName; }
    public void setReportName(String reportName) {
        this.reportName = reportName == null ? null : reportName.trim();
    }

    public String getCategoryName() { return this.categoryName; }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName == null ? null : categoryName.trim();
    }

}
