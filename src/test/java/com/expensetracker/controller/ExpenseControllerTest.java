package com.expensetracker.controller;

import com.expensetracker.dto.ExpenseResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.exception.NotFoundException;
import com.expensetracker.service.ExpenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-layer tests: real MVC stack (validation, serialization, error handling),
 * mocked service. The unit tests cover the business rules; these cover the
 * HTTP contract.
 */
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService service;

    @Test
    void postValidExpenseReturns201WithLocationHeader() throws Exception {
        when(service.create(any())).thenReturn(new ExpenseResponse(
                1L, "Coffee", new BigDecimal("4.50"), Category.FOOD,
                LocalDate.of(2026, 11, 17), Instant.parse("2026-11-17T09:00:00Z")));

        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "Coffee", "amount": 4.50, "category": "FOOD", "date": "2026-11-17"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/expenses/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    void postInvalidExpenseReturns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description": "", "amount": -5, "category": null, "date": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.fieldErrors.description").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.amount").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.category").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.date").isNotEmpty());
    }

    @Test
    void getUnknownExpenseReturns404WithErrorBody() throws Exception {
        when(service.getById(99L)).thenThrow(new NotFoundException("Expense 99 not found"));

        mockMvc.perform(get("/api/v1/expenses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Expense 99 not found"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
