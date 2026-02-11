package com.example.project1.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SmsService {

    private static final Logger logger = Logger.getLogger(SmsService.class.getName());

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    /**
     * Initialize Twilio only once when application starts
     */
    @PostConstruct
    public void init() {
        if (isConfigured()) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialized successfully.");
        } else {
            logger.warning("Twilio credentials not configured properly.");
        }
    }

    /**
     * Send SMS to candidate after applying
     */
    public boolean sendApplicationConfirmationSms(String recipientPhoneNumber,
                                                  String jobTitle,
                                                  String companyName) {
        try {
            if (!isConfigured()) {
                logger.warning("SMS service not configured. Skipping SMS send.");
                return false;
            }

            String messageBody = buildCandidateMessage(jobTitle, companyName);

            Message message = Message.creator(
                    new PhoneNumber(formatPhoneNumber(recipientPhoneNumber)),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            logger.info("SMS sent successfully to candidate. SID: " + message.getSid());
            return true;

        } catch (Exception e) {
            logger.severe("Error sending SMS to candidate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send SMS to employer when new candidate applies
     */
    public boolean sendApplicationToEmployerSms(String employerPhoneNumber,
                                                String candidateName,
                                                String jobTitle) {
        try {
            if (!isConfigured()) {
                logger.warning("SMS service not configured. Skipping SMS send.");
                return false;
            }

            String messageBody = buildEmployerMessage(candidateName, jobTitle);

            Message message = Message.creator(
                    new PhoneNumber(formatPhoneNumber(employerPhoneNumber)),
                    new PhoneNumber(twilioPhoneNumber),
                    messageBody
            ).create();

            logger.info("SMS sent successfully to employer. SID: " + message.getSid());
            return true;

        } catch (Exception e) {
            logger.severe("Error sending SMS to employer: " + e.getMessage());
            return false;
        }
    }

    /**
     * Format and validate phone number (E.164 required)
     */
    private String formatPhoneNumber(String phoneNumber) {

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        phoneNumber = phoneNumber.trim();
        phoneNumber = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Require country code
        if (!phoneNumber.startsWith("+")) {
            throw new IllegalArgumentException(
                    "Phone number must include country code. Example: +14155552671");
        }

        // Validate E.164 format
        if (!phoneNumber.matches("^\\+[1-9]\\d{1,14}$")) {
            throw new IllegalArgumentException(
                    "Phone number must be in valid E.164 format. Example: +14155552671");
        }

        return phoneNumber;
    }

    /**
     * Build candidate confirmation message
     */
    private String buildCandidateMessage(String jobTitle, String companyName) {
        return String.format(
                "Thank you for applying! Your application for %s at %s has been received. We will review and get back to you soon.",
                jobTitle,
                companyName
        );
    }

    /**
     * Build employer notification message
     */
    private String buildEmployerMessage(String candidateName, String jobTitle) {
        return String.format(
                "New application received! %s has applied for %s. Login to review the application.",
                candidateName,
                jobTitle
        );
    }

    /**
     * Check if Twilio is properly configured
     */
    private boolean isConfigured() {
        return accountSid != null && !accountSid.isEmpty()
                && authToken != null && !authToken.isEmpty()
                && twilioPhoneNumber != null && !twilioPhoneNumber.isEmpty()
                && !accountSid.equals("your_twilio_account_sid");
    }
}
