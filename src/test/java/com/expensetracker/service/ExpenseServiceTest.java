package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.exception.NotFoundException;
import com.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for the business rules — no Spring context, fast feedback.
 */
@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository repository;

    private ExpenseService service;

    @BeforeEach
    void setUp() {
        service = new ExpenseService(repository);
    }

    @Test
    void createMapsRequestAndSaves() {
        ExpenseRequest request = new ExpenseRequest(
                "Coffee", new BigDecimal("4.50"), Category.FOOD, LocalDate.of(2026, 11, 17));
        Expense saved = new Expense("Coffee", new BigDecimal("4.50"), Category.FOOD, LocalDate.of(2026, 11, 17));
        when(repository.save(any(Expense.class))).thenAnswer(invocation -> {
            Expense toSave = invocation.getArgument(0);
            toSave.setId(42L);
            return toSave;
        });

        ExpenseResponse response = service.create(request);

        assertEquals(42L, response.id());
        assertEquals("Coffee", response.description());
        assertEquals(Category.FOOD, response.category());
        assertEquals(new BigDecimal("4.50"), response.amount());
    }

    @Test
    void getByIdThrowsNotFoundForUnknownId() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void monthlySummaryAggregatesDatabaseTotals() {
        YearMonth month = YearMonth.of(2026, 11);
        when(repository.sumAmountBetween(month.atDay(1), month.plusMonths(1).atDay(1)))
                .thenReturn(new BigDecimal("195.90"));
        when(repository.sumByCategoryBetween(month.atDay(1), month.plusMonths(1).atDay(1)))
                .thenReturn(List.of(
                        new CategorySumRow(Category.HOUSING, new BigDecimal("150.00")),
                        new CategorySumRow(Category.FOOD, new BigDecimal("45.90"))));

        MonthlySummaryResponse summary = service.monthlySummary(month);

        assertEquals("2026-11", summary.month());
        assertEquals(new BigDecimal("195.90"), summary.total());
        assertEquals(2, summary.byCategory().size());
        assertEquals(Category.HOUSING, summary.byCategory().get(0).category(), "highest total first");
    }

    @Test
    void deleteThrowsNotFoundForUnknownId() {
        when(repository.existsById(7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(7L));
    }

    /** Test double for the repository's interface projection; record accessors implement it directly. */
    private record CategorySumRow(Category category, BigDecimal total) implements ExpenseRepository.CategorySum {
    }
}
