package com.unilink.controller;

import com.unilink.service.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestEmailController {
    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public String testEmail(@RequestParam String to) {
        try {
            emailService.sendEmail(to, "Test Email", "This is a test email from Student Affairs System");
            return "Email sent successfully to: " + to;
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}