package com.fintrack.service.impl;

import com.fintrack.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            logFallback(to, subject, body, null);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("no-reply@fintrack.com");
            mailSender.send(message);
            logger.info("Sent email successfully to: {}", to);
        } catch (Exception e) {
            logFallback(to, subject, body, e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            logFallbackHtml(to, subject, htmlContent, null);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("no-reply@fintrack.com");
            mailSender.send(mimeMessage);
            logger.info("Sent HTML email successfully to: {}", to);
        } catch (Exception e) {
            logFallbackHtml(to, subject, htmlContent, e);
        }
    }

    private void logFallback(String to, String subject, String body, Exception exception) {
        String reason = (exception != null) ? exception.getMessage() : "JavaMailSender not configured";
        logger.warn("[FinTrack Email Fallback] Reason: {}. Email logged to console:\n" +
                "--------------------------------------------------\n" +
                "TO: {}\n" +
                "SUBJECT: {}\n" +
                "BODY:\n{}\n" +
                "--------------------------------------------------", 
                reason, to, subject, body);
    }

    private void logFallbackHtml(String to, String subject, String htmlContent, Exception exception) {
        String reason = (exception != null) ? exception.getMessage() : "JavaMailSender not configured";
        logger.warn("[FinTrack HTML Email Fallback] Reason: {}. Email logged to console:\n" +
                "--------------------------------------------------\n" +
                "TO: {}\n" +
                "SUBJECT: {}\n" +
                "HTML CONTENT:\n{}\n" +
                "--------------------------------------------------", 
                reason, to, subject, htmlContent);
    }
}
