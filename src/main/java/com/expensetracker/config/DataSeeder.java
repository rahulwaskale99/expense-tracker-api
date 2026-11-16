package com.expensetracker.config;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.repository.ExpenseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a few months of plausible expenses so the API has data to show the
 * moment it starts in the dev profile. Real deployments start empty.
 */
@Component
public class DataSeeder {

    @Bean
    @Profile("dev")
    CommandLineRunner seedExpenses(ExpenseRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return; // idempotent: never duplicate on restart
            }
            LocalDate today = LocalDate.now();
            List<Expense> samples = List.of(
                    new Expense("Grocery weekly run", new BigDecimal("86.40"), Category.FOOD, today.minusDays(2)),
                    new Expense("Coffee with team", new BigDecimal("12.50"), Category.FOOD, today.minusDays(5)),
                    new Expense("Monthly metro pass", new BigDecimal("65.00"), Category.TRANSPORT, today.minusDays(20)),
                    new Expense("Uber to airport", new BigDecimal("28.90"), Category.TRANSPORT, today.minusDays(34)),
                    new Expense("Apartment rent", new BigDecimal("950.00"), Category.HOUSING, today.minusDays(28)),
                    new Expense("Electricity bill", new BigDecimal("74.20"), Category.HOUSING, today.minusDays(12)),
                    new Expense("Cinema tickets", new BigDecimal("24.00"), Category.ENTERTAINMENT, today.minusDays(8)),
                    new Expense("Streaming subscription", new BigDecimal("15.99"), Category.ENTERTAINMENT, today.minusDays(40)),
                    new Expense("Pharmacy", new BigDecimal("31.75"), Category.HEALTH, today.minusDays(6)),
                    new Expense("Dentist checkup", new BigDecimal("120.00"), Category.HEALTH, today.minusDays(25)),
                    new Expense("Paperback novel", new BigDecimal("14.99"), Category.OTHER, today.minusDays(3)),
                    new Expense("Gift for friend", new BigDecimal("45.00"), Category.OTHER, today.minusDays(15)));
            repository.saveAll(samples);
        };
    }
}
