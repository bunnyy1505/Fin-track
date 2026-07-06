package com.fintrack.service;

import com.fintrack.entity.User;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;

public interface ReportService {
    ByteArrayInputStream generateCSVReport(User user, LocalDate startDate, LocalDate endDate);
    ByteArrayInputStream generatePDFReport(User user, LocalDate startDate, LocalDate endDate);
}
