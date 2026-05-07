package com.example.authservice.util;

import com.example.authservice.dto.event.EmailSendEvent;
import com.example.authservice.entity.Outbox;
import com.example.authservice.mapper.EmailSendMapper;
import com.example.authservice.producer.EmailSendProducer;
import com.example.authservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    public static final int LIMIT = 5;

    private final EmailSendProducer emailSendProducer;
    private final OutboxRepository outboxRepository;
    private final EmailSendMapper emailSendMapper;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    void sendMessages() {

        List<Outbox> pendingEvents = outboxRepository.findPendingEvents(LIMIT);

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (Outbox event : pendingEvents) {

            EmailSendEvent emailSendEvent = mapToDto(event.getPayload());

            emailSendProducer.sendEmails(emailSendEvent);
            event.setSentAt(Instant.now());
            log.info("Отправил сообщение {}", event);
        }

    }

    private EmailSendEvent mapToDto(String payload){
        JsonNode node = objectMapper.readTree(payload);

        // Достаем code с верхнего уровня
        String code = node.get("code").asString();

        // Достаем email из вложенного объекта user
        String email = node.get("user").get("email").asString();

        return new EmailSendEvent(email, code);
    }
}
