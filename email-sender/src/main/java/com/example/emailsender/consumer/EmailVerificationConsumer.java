package com.example.emailsender.consumer;

import com.example.emailsender.consumer.dto.event.EmailVerificationEvent;
import com.example.emailsender.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationConsumer {

    private final ObjectMapper objectMapper;
    private final EmailVerificationService emailVerificationService;

    @KafkaListener(topics = "verification-code-topic", groupId = "email-sender-group")
    public void emailSendConsumer(String message) {
            EmailVerificationEvent emailVerificationEvent = objectMapper.readValue(message, EmailVerificationEvent.class);
            log.info("Получил {}", emailVerificationEvent);
            emailVerificationService.send(emailVerificationEvent.email(), "Ваш код", emailVerificationEvent.code());
            log.info("Код успешно отпрввлен на почту {}", emailVerificationEvent.email());
    }

}
