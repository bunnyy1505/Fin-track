package com.fintrack.controller;

import com.fintrack.dto.RecurringTransactionRequest;
import com.fintrack.dto.RecurringTransactionResponse;
import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.response.ApiResponse;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.RecurringTransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recurring")
public class RecurringTransactionController {

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> createRecurring(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RecurringTransactionRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        RecurringTransactionResponse response = recurringTransactionService.createRecurringTransaction(user, request);
        return new ResponseEntity<>(new ApiResponse<>(HttpStatus.CREATED.value(), "Recurring transaction created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getRecurringById(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        RecurringTransactionResponse response = recurringTransactionService.getRecurringTransactionById(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched recurring transaction", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringTransactionResponse>>> getAllRecurring(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = getAuthenticatedUser(userPrincipal);
        List<RecurringTransactionResponse> list = recurringTransactionService.getAllRecurringTransactions(user);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched recurring transactions successfully", list));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> updateRecurring(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @Valid @RequestBody RecurringTransactionRequest request) {
        User user = getAuthenticatedUser(userPrincipal);
        RecurringTransactionResponse response = recurringTransactionService.updateRecurringTransaction(user, id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Recurring transaction updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRecurring(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        User user = getAuthenticatedUser(userPrincipal);
        recurringTransactionService.deleteRecurringTransaction(user, id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Recurring transaction deleted successfully", "Deleted"));
    }
}
