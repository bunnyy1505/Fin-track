package com.fintrack.controller;

import com.fintrack.dto.ExpenseRequest;
import com.fintrack.dto.ExpenseResponse;
import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ExpenseRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        ExpenseResponse response = expenseService.createExpense(user, request);
        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.CREATED.value(), "Expense created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        ExpenseResponse response = expenseService.getExpenseById(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched expense record", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExpenseResponse>>> searchExpenses(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        User user = getAuthenticatedUser(userPrincipal);
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExpenseResponse> results = expenseService.searchExpenses(user, startDate, endDate, category, search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Expenses fetched successfully", results));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        ExpenseResponse response = expenseService.updateExpense(user, id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Expense updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteExpense(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        expenseService.deleteExpense(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Expense record deleted successfully", "Deleted"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlySummary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        User user = getAuthenticatedUser(userPrincipal);
        LocalDate now = LocalDate.now();
        int y = (year != null) ? year : now.getYear();
        int m = (month != null) ? month : now.getMonthValue();

        LocalDate start = LocalDate.of(y, m, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<ExpenseResponse> expenses = expenseService.getExpensesForPeriod(user, start, end);
        BigDecimal total = expenses.stream().map(ExpenseResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("year", y);
        data.put("month", m);
        data.put("totalExpense", total);
        data.put("recordCount", expenses.size());

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched monthly summary", data));
    }
}
