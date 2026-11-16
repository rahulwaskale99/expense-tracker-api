package com.expensetracker.dto;

import com.expensetracker.entity.Category;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Outgoing representation of an expense; the API never exposes the JPA entity directly. */
public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        Category category,
        LocalDate date,
        Instant createdAt) {
}
