package com.fintrack.controller;

import com.fintrack.entity.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.security.UserPrincipal;
import com.fintrack.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserRepository userRepository;

    private User getAuthenticatedUser(UserPrincipal principal) {
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("Current authenticated user not found in database"));
    }

    @GetMapping("/csv")
    public ResponseEntity<InputStreamResource> downloadCSV(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getAuthenticatedUser(userPrincipal);
        ByteArrayInputStream bis = reportService.generateCSVReport(user, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=fintrack_report_" + startDate + "_to_" + endDate + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/pdf")
    public ResponseEntity<InputStreamResource> downloadPDF(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        User user = getAuthenticatedUser(userPrincipal);
        ByteArrayInputStream bis = reportService.generatePDFReport(user, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=fintrack_report_" + startDate + "_to_" + endDate + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}
