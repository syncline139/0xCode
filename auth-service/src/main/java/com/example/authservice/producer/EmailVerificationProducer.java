package com.example.authservice.producer;

import com.example.authservice.dto.event.EmailVerificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendEmails(EmailVerificationEvent emailSendEvent) {
        try {
            kafkaTemplate.send("verification-code-topic", emailSendEvent).get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send message to Kafka. {}", e.getMessage());
            throw new RuntimeException("Failed to send message to Kafka", e);
        }
        log.info("EmailSendEvent {}", emailSendEvent);
    }
}
