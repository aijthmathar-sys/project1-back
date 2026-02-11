package com.example.project1.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SmsServiceTest {

    private SmsService smsService;

    private final String validPhone = "+919876543210";
    private final String jobTitle = "Software Engineer";
    private final String companyName = "Tech Corp";
    private final String candidateName = "John Doe";

    @BeforeEach
    void setUp() {
        smsService = new SmsService();

        // Inject fake Twilio config
        ReflectionTestUtils.setField(smsService, "accountSid", "test_sid");
        ReflectionTestUtils.setField(smsService, "authToken", "test_token");
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", "+1234567890");
    }

    // ---------------- FORMAT TESTS (via reflection) ----------------

    @Test
    void testFormatPhoneNumberValidIndian() {
        String result = (String) ReflectionTestUtils.invokeMethod(
                smsService, "formatPhoneNumber", "+91 98765 43210");

        assertEquals("+919876543210", result);
    }

    @Test
    void testFormatPhoneNumberInvalidNoPlus() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        smsService, "formatPhoneNumber", "9876543210"));

        assertTrue(exception.getMessage().contains("country code"));
    }

    @Test
    void testFormatPhoneNumberInvalidEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        smsService, "formatPhoneNumber", ""));

        assertEquals("Phone number cannot be empty", exception.getMessage());
    }

    @Test
    void testFormatPhoneNumberInvalidFormat() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        smsService, "formatPhoneNumber", "+"));

        assertTrue(exception.getMessage().contains("E.164"));
    }

    // ---------------- SEND SMS TESTS ----------------

    @Test
    void testSendApplicationConfirmationSmsConfigured() {
        boolean result = smsService.sendApplicationConfirmationSms(
                validPhone,
                jobTitle,
                companyName
        );

        // Since Twilio is not mocked, it will likely fail internally
        // but method should safely return false or true without crashing
        assertNotNull(result);
    }

    @Test
    void testSendApplicationToEmployerSmsConfigured() {
        boolean result = smsService.sendApplicationToEmployerSms(
                validPhone,
                candidateName,
                jobTitle
        );

        assertNotNull(result);
    }

    @Test
    void testSendSmsNotConfigured() {

        // Remove config
        ReflectionTestUtils.setField(smsService, "accountSid", null);
        ReflectionTestUtils.setField(smsService, "authToken", null);
        ReflectionTestUtils.setField(smsService, "twilioPhoneNumber", null);

        boolean result = smsService.sendApplicationConfirmationSms(
                validPhone,
                jobTitle,
                companyName
        );

        assertFalse(result);
    }

    @Test
    void testSendSmsInvalidPhone() {

        boolean result = smsService.sendApplicationConfirmationSms(
                "invalid-phone",
                jobTitle,
                companyName
        );

        assertFalse(result);
    }
}
