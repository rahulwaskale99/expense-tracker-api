package com.expensetracker.dto;

import com.expensetracker.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Incoming payload for creating/updating an expense.
 *
 * <p>{@code date} must not be in the future — tracking an expense for a day
 * that has not happened yet is almost certainly a typo of the year.
 */
public record ExpenseRequest(
        @NotBlank(message = "description is required")
        String description,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "category is required")
        Category category,

        @NotNull(message = "date is required")
        @PastOrPresent(message = "date cannot be in the future")
        LocalDate date) {
}
