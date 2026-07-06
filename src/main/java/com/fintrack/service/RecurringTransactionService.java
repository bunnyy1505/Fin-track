package com.fintrack.service;

import com.fintrack.dto.RecurringTransactionRequest;
import com.fintrack.dto.RecurringTransactionResponse;
import com.fintrack.entity.User;
import java.util.List;

public interface RecurringTransactionService {
    RecurringTransactionResponse createRecurringTransaction(User user, RecurringTransactionRequest request);
    RecurringTransactionResponse getRecurringTransactionById(User user, Long id);
    List<RecurringTransactionResponse> getAllRecurringTransactions(User user);
    RecurringTransactionResponse updateRecurringTransaction(User user, Long id, RecurringTransactionRequest request);
    void deleteRecurringTransaction(User user, Long id);
    void processRecurringTransactions();
}
