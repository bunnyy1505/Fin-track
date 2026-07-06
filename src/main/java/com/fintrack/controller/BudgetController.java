package com.fintrack.controller;

import com.fintrack.dto.BudgetRequest;
import com.fintrack.dto.BudgetResponse;
import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody BudgetRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        BudgetResponse response = budgetService.createBudget(user, request);
        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.CREATED.value(), "Budget created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        BudgetResponse response = budgetService.getBudgetById(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched budget", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getAllBudgets(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getAuthenticatedUser(userPrincipal);
        List<BudgetResponse> budgets = budgetService.getAllBudgets(user);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched budgets successfully", budgets));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        BudgetResponse response = budgetService.updateBudget(user, id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBudget(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        budgetService.deleteBudget(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Budget deleted successfully", "Deleted"));
    }
}
