package com.fintrack.scheduler;

import com.fintrack.service.RecurringTransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "fintrack.recurring.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RecurringTransactionScheduler {
    private static final Logger logger = LoggerFactory.getLogger(RecurringTransactionScheduler.class);

    @Autowired
    private RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void runRecurringTransactions() {
        logger.info("Executing scheduled recurring transactions check...");
        recurringTransactionService.processRecurringTransactions();
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 86400000)
    public void runInitialCheck() {
        logger.info("Executing initial check for recurring transactions...");
        recurringTransactionService.processRecurringTransactions();
    }
}
