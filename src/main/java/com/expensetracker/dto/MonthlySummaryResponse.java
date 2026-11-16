package com.expensetracker.dto;

import com.expensetracker.entity.Category;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aggregated spending for one month.
 *
 * @param month      yyyy-MM
 * @param total      grand total across all categories
 * @param byCategory per-category totals, highest first; categories with no
 *                   spending are omitted
 */
public record MonthlySummaryResponse(String month, BigDecimal total, List<CategoryTotal> byCategory) {

    public record CategoryTotal(Category category, BigDecimal total) {
    }
}
