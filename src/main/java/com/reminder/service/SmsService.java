package com.reminder.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        // Only init if real credentials are provided
        if (!accountSid.startsWith("AC")) {
            log.warn("Twilio not configured — SMS notifications disabled");
            return;
        }
        Twilio.init(accountSid, authToken);
    }

    public void sendSms(String toNumber, String message) {
        if (!accountSid.startsWith("AC")) {
            log.info("SMS skipped (Twilio not configured): {}", message);
            return;
        }
        try {
            Message.creator(
                    new PhoneNumber(toNumber),
                    new PhoneNumber(fromNumber),
                    message
            ).create();
            log.info("SMS sent to {}", toNumber);
        } catch (Exception e) {
            log.error("SMS failed to {}: {}", toNumber, e.getMessage());
        }
    }
}
