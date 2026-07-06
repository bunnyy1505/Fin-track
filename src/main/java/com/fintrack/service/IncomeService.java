package com.fintrack.service;

import com.fintrack.dto.IncomeRequest;
import com.fintrack.dto.IncomeResponse;
import com.fintrack.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
    IncomeResponse createIncome(User user, IncomeRequest incomeRequest);
    IncomeResponse getIncomeById(User user, Long id);
    Page<IncomeResponse> getAllIncomes(User user, Pageable pageable);
    Page<IncomeResponse> searchIncomes(User user, LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable);
    IncomeResponse updateIncome(User user, Long id, IncomeRequest incomeRequest);
    void deleteIncome(User user, Long id);
    List<IncomeResponse> getIncomesForPeriod(User user, LocalDate startDate, LocalDate endDate);
}
