package com.fintrack.service.impl;

import com.fintrack.dto.RecurringTransactionRequest;
import com.fintrack.dto.RecurringTransactionResponse;
import com.fintrack.entity.Expense;
import com.fintrack.entity.Income;
import com.fintrack.entity.RecurringTransaction;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.IncomeRepository;
import com.fintrack.repository.RecurringTransactionRepository;
import com.fintrack.service.AuditLogService;
import com.fintrack.service.NotificationService;
import com.fintrack.service.RecurringTransactionService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(RecurringTransactionServiceImpl.class);

    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public RecurringTransactionResponse createRecurringTransaction(User user, RecurringTransactionRequest request) {
        RecurringTransaction rt = modelMapper.map(request, RecurringTransaction.class);
        rt.setUser(user);
        rt.setActive(true);
        RecurringTransaction saved = recurringTransactionRepository.save(rt);

        auditLogService.logAction(user, "CREATE_RECURRING", "Added recurring " + saved.getType() + " frequency=" + saved.getFrequency(), "0.0.0.0");

        return modelMapper.map(saved, RecurringTransactionResponse.class);
    }

    @Override
    public RecurringTransactionResponse getRecurringTransactionById(User user, Long id) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id " + id));
        return modelMapper.map(rt, RecurringTransactionResponse.class);
    }

    @Override
    public List<RecurringTransactionResponse> getAllRecurringTransactions(User user) {
        return recurringTransactionRepository.findByUser(user).stream()
                .map(r -> modelMapper.map(r, RecurringTransactionResponse.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringTransactionResponse updateRecurringTransaction(User user, Long id, RecurringTransactionRequest request) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id " + id));

        rt.setType(request.getType());
        rt.setAmount(request.getAmount());
        rt.setCategory(request.getCategory());
        rt.setDescription(request.getDescription());
        rt.setFrequency(request.getFrequency());
        rt.setNextExecutionDate(request.getNextExecutionDate());

        RecurringTransaction updated = recurringTransactionRepository.save(rt);
        auditLogService.logAction(user, "UPDATE_RECURRING", "Modified recurring transaction: ID=" + id, "0.0.0.0");

        return modelMapper.map(updated, RecurringTransactionResponse.class);
    }

    @Override
    @Transactional
    public void deleteRecurringTransaction(User user, Long id) {
        RecurringTransaction rt = recurringTransactionRepository.findById(id)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Recurring transaction not found with id " + id));

        rt.setActive(false);
        recurringTransactionRepository.save(rt);
        auditLogService.logAction(user, "DELETE_RECURRING", "Deactivated recurring transaction: ID=" + id, "0.0.0.0");
    }

    @Override
    @Transactional
    public void processRecurringTransactions() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTransactions = recurringTransactionRepository
                .findDueTransactions(today);

        logger.info("Scheduler: Found {} due recurring transactions to execute", dueTransactions.size());

        for (RecurringTransaction rt : dueTransactions) {
            try {
                User user = rt.getUser();
                if ("INCOME".equalsIgnoreCase(rt.getType())) {
                    Income income = Income.builder()
                            .user(user)
                            .amount(rt.getAmount())
                            .category(rt.getCategory())
                            .description("[Recurring] " + rt.getDescription())
                            .transactionDate(rt.getNextExecutionDate())
                            .isDeleted(false)
                            .build();
                    incomeRepository.save(income);
                } else {
                    Expense expense = Expense.builder()
                            .user(user)
                            .amount(rt.getAmount())
                            .category(rt.getCategory())
                            .description("[Recurring] " + rt.getDescription())
                            .transactionDate(rt.getNextExecutionDate())
                            .isDeleted(false)
                            .build();
                    expenseRepository.save(expense);
                }

                rt.setLastExecutionDate(rt.getNextExecutionDate());
                rt.setNextExecutionDate(calculateNextDate(rt.getNextExecutionDate(), rt.getFrequency()));
                recurringTransactionRepository.save(rt);

                String message = String.format("Scheduler: Automatically inserted recurring %s of %s under category '%s'", 
                        rt.getType().toLowerCase(), rt.getAmount(), rt.getCategory());
                notificationService.createNotification(user, message, "UPCOMING_RECURRING");

                logger.info("Executed recurring transaction ID: {}", rt.getId());
            } catch (Exception e) {
                logger.error("Error executing recurring transaction ID: {}", rt.getId(), e);
            }
        }
    }

    private LocalDate calculateNextDate(LocalDate current, String frequency) {
        return switch (frequency.toUpperCase()) {
            case "DAILY" -> current.plusDays(1);
            case "WEEKLY" -> current.plusWeeks(1);
            case "MONTHLY" -> current.plusMonths(1);
            default -> current.plusMonths(1);
        };
    }
}
