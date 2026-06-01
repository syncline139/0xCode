package com.example.authservice.producer;

import com.example.authservice.dto.event.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmails(EmailVerificationEvent emailSendEvent) {
        kafkaTemplate.send("verification-code-topic", emailSendEvent);
        log.info("EmailSendEvent {}", emailSendEvent);
    }
}
