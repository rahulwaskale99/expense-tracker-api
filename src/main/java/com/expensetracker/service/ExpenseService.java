package com.expensetracker.service;

import com.expensetracker.dto.ExpenseRequest;
import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.dto.MonthlySummaryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.exception.NotFoundException;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Business rules for expenses. The transaction boundary sits here, not in the
 * controller, so every public method runs atomically regardless of who calls it.
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = new Expense(
                request.description(), request.amount(), request.category(), request.date());
        return toResponse(repository.save(expense));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Expense " + id + " not found"));
    }

    /**
     * Lists expenses with optional filters. Kept as explicit branches instead
     * of one mega-query so each case hits a repository method with a clear,
     * type-safe signature.
     */
    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Category category, LocalDate fromDate, LocalDate toDate) {
        if (category == null && fromDate == null && toDate == null) {
            return repository.findAll().stream().map(this::toResponse).toList();
        }
        LocalDate start = fromDate != null ? fromDate : LocalDate.of(1970, 1, 1);
        LocalDate end = toDate != null ? toDate : LocalDate.of(9999, 12, 31);
        List<Expense> expenses = category != null
                ? repository.findByCategoryAndDateBetween(category, start, end)
                : repository.findByDateBetween(start, end);
        return expenses.stream().map(this::toResponse).toList();
    }

    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense " + id + " not found"));
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setDate(request.date());
        return toResponse(expense); // managed entity, flushed at commit
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Expense " + id + " not found");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse monthlySummary(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate endExclusive = month.plusMonths(1).atDay(1);

        BigDecimal total = repository.sumAmountBetween(start, endExclusive);
        List<MonthlySummaryResponse.CategoryTotal> byCategory = repository.sumByCategoryBetween(start, endExclusive)
                .stream()
                .map(row -> new MonthlySummaryResponse.CategoryTotal(row.getCategory(), row.getTotal()))
                .toList();

        return new MonthlySummaryResponse(month.toString(), total, byCategory);
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getCreatedAt());
    }
}
