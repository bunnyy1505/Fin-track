package com.fintrack.service.impl;

import com.fintrack.entity.Expense;
import com.fintrack.entity.Income;
import com.fintrack.entity.User;
import com.fintrack.repository.ExpenseRepository;
import com.fintrack.repository.IncomeRepository;
import com.fintrack.service.ReportService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public ByteArrayInputStream generateCSVReport(User user, LocalDate startDate, LocalDate endDate) {
        List<Income> incomes = incomeRepository.findByUserAndTransactionDateBetween(user, startDate, endDate);
        List<Expense> expenses = expenseRepository.findByUserAndTransactionDateBetween(user, startDate, endDate);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.withHeader("Date", "Type", "Category", "Amount", "Description"))) {
            for (Income inc : incomes) {
                csvPrinter.printRecord(inc.getTransactionDate(), "INCOME", inc.getCategory(), inc.getAmount(), inc.getDescription());
            }
            for (Expense exp : expenses) {
                csvPrinter.printRecord(exp.getTransactionDate(), "EXPENSE", exp.getCategory(), exp.getAmount(), exp.getDescription());
            }
            csvPrinter.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV file: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream generatePDFReport(User user, LocalDate startDate, LocalDate endDate) {
        List<Income> incomes = incomeRepository.findByUserAndTransactionDateBetween(user, startDate, endDate);
        List<Expense> expenses = expenseRepository.findByUserAndTransactionDateBetween(user, startDate, endDate);

        BigDecimal totalIncome = incomes.stream().map(Income::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenses.stream().map(Expense::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("FinTrack Financial Statement");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);
                contentStream.showText("User: " + user.getFullName() + " (" + user.getUsername() + ")");
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Report Period: " + startDate + " to " + endDate);
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Generated: " + LocalDate.now());
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("SUMMARY:");
                contentStream.newLineAtOffset(20, -15);
                contentStream.showText("Total Income: $" + totalIncome);
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Total Expense: $" + totalExpense);
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Net Savings: $" + totalIncome.subtract(totalExpense));
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.newLineAtOffset(50, 580);
                contentStream.showText("Transactions (Summary):");
                contentStream.endText();

                int yPosition = 550;
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                
                int counter = 0;
                for (Income inc : incomes) {
                    if (counter >= 10) break;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    String desc = inc.getDescription() != null ? inc.getDescription() : "";
                    desc = desc.length() > 30 ? desc.substring(0, 27) + "..." : desc;
                    contentStream.showText(inc.getTransactionDate() + " | INCOME | " + inc.getCategory() + " | +$" + inc.getAmount() + " | " + desc);
                    contentStream.endText();
                    yPosition -= 15;
                    counter++;
                }

                for (Expense exp : expenses) {
                    if (counter >= 20) break;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    String desc = exp.getDescription() != null ? exp.getDescription() : "";
                    desc = desc.length() > 30 ? desc.substring(0, 27) + "..." : desc;
                    contentStream.showText(exp.getTransactionDate() + " | EXPENSE | " + exp.getCategory() + " | -$" + exp.getAmount() + " | " + desc);
                    contentStream.endText();
                    yPosition -= 15;
                    counter++;
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error during PDF creation: " + e.getMessage());
        }
    }
}
