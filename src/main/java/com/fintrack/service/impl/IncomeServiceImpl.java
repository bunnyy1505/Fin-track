package com.fintrack.service.impl;

import com.fintrack.dto.IncomeRequest;
import com.fintrack.dto.IncomeResponse;
import com.fintrack.entity.Income;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.IncomeRepository;
import com.fintrack.service.AuditLogService;
import com.fintrack.service.IncomeService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IncomeServiceImpl implements IncomeService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    @Transactional
    public IncomeResponse createIncome(User user, IncomeRequest request) {
        Income income = modelMapper.map(request, Income.class);
        income.setUser(user);
        income.setDeleted(false);
        Income saved = incomeRepository.save(income);

        auditLogService.logAction(user, "CREATE_INCOME", "Added income: " + saved.getAmount() + " in " + saved.getCategory(), "0.0.0.0");

        return modelMapper.map(saved, IncomeResponse.class);
    }

    @Override
    public IncomeResponse getIncomeById(User user, Long id) {
        Income income = incomeRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(user.getId()) && !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id " + id));
        return modelMapper.map(income, IncomeResponse.class);
    }

    @Override
    public Page<IncomeResponse> getAllIncomes(User user, Pageable pageable) {
        Page<Income> incomes = incomeRepository.findByUserAndIsDeletedFalse(user, pageable);
        return incomes.map(i -> modelMapper.map(i, IncomeResponse.class));
    }

    @Override
    public Page<IncomeResponse> searchIncomes(User user, LocalDate startDate, LocalDate endDate, String category, String search, Pageable pageable) {
        if (startDate == null) startDate = LocalDate.of(1970, 1, 1);
        if (endDate == null) endDate = LocalDate.of(2099, 12, 31);
        
        String cat = (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("all")) ? null : category;
        String query = (search == null || search.trim().isEmpty()) ? null : search;

        Page<Income> incomes = incomeRepository.searchIncomes(user, startDate, endDate, cat, query, pageable);
        return incomes.map(i -> modelMapper.map(i, IncomeResponse.class));
    }

    @Override
    @Transactional
    public IncomeResponse updateIncome(User user, Long id, IncomeRequest request) {
        Income income = incomeRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(user.getId()) && !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id " + id));

        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDescription(request.getDescription());
        income.setTransactionDate(request.getTransactionDate());
        Income updated = incomeRepository.save(income);

        auditLogService.logAction(user, "UPDATE_INCOME", "Modified income: ID=" + id + " amount=" + updated.getAmount(), "0.0.0.0");

        return modelMapper.map(updated, IncomeResponse.class);
    }

    @Override
    @Transactional
    public void deleteIncome(User user, Long id) {
        Income income = incomeRepository.findById(id)
                .filter(i -> i.getUser().getId().equals(user.getId()) && !i.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Income not found with id " + id));

        income.setDeleted(true);
        incomeRepository.save(income);

        auditLogService.logAction(user, "DELETE_INCOME", "Deleted income: ID=" + id, "0.0.0.0");
    }

    @Override
    public List<IncomeResponse> getIncomesForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserAndTransactionDateBetween(user, startDate, endDate).stream()
                .map(i -> modelMapper.map(i, IncomeResponse.class))
                .collect(Collectors.toList());
    }
}
