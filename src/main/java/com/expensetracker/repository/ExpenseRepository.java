package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategoryAndDateBetween(Category category, LocalDate start, LocalDate end);

    List<Expense> findByDateBetween(LocalDate start, LocalDate end);

    /** Grand total spent in [start, endExclusive). coalesce keeps empty months at zero. */
    @Query("select coalesce(sum(e.amount), 0) from Expense e "
            + "where e.date >= :start and e.date < :endExclusive")
    BigDecimal sumAmountBetween(@Param("start") LocalDate start, @Param("endExclusive") LocalDate endExclusive);

    /**
     * Per-category totals for one month. Interface projection: the query
     * aggregates in the database and Spring maps the (category, total) tuples
     * onto {@link CategorySum} — no entities are loaded.
     */
    @Query("select e.category as category, sum(e.amount) as total from Expense e "
            + "where e.date >= :start and e.date < :endExclusive "
            + "group by e.category order by total desc")
    List<CategorySum> sumByCategoryBetween(@Param("start") LocalDate start,
                                           @Param("endExclusive") LocalDate endExclusive);

    /**
     * Read-only view of one aggregated row. Spring Data interface projections
     * require getter-style method names matching the query aliases
     * (getCategory ↔ ... as category).
     */
    interface CategorySum {
        Category getCategory();

        BigDecimal getTotal();
    }
}
