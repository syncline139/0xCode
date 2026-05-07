package com.example.emailsender.consumer;

import com.example.emailsender.consumer.dto.event.EmailSendEvent;
import com.example.emailsender.service.MailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailSendConsumer {

    private final ObjectMapper objectMapper;
    private final MailSenderService mailSenderService;

    @KafkaListener(topics = "verification-code-topic", groupId = "email-sender-group")
    public void emailSendConsumer(String message) { // Принимаем строку
            EmailSendEvent emailSendEvent = objectMapper.readValue(message, EmailSendEvent.class);
            log.info("Получил {}", emailSendEvent);
            mailSenderService.send(emailSendEvent.email(), "Ваш код", emailSendEvent.code());
            log.info("Код успешно отпрввлен на почту {}", emailSendEvent.email());
    }

}
