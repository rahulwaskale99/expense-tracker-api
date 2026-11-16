package com.expensetracker.entity;

/**
 * Fixed expense categories. Kept as an enum rather than free text so the
 * monthly summary can aggregate reliably and typos cannot create orphan
 * categories.
 */
public enum Category {
    FOOD,
    TRANSPORT,
    HOUSING,
    ENTERTAINMENT,
    HEALTH,
    OTHER
}
