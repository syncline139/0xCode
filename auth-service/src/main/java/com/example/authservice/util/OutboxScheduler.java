package com.example.authservice.util;

import com.example.authservice.constant.OutboxStatus;
import com.example.authservice.dto.event.EmailVerificationEvent;
import com.example.authservice.entity.Outbox;
import com.example.authservice.producer.EmailVerificationProducer;
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

    private static final int LIMIT = 5;

    private final EmailVerificationProducer emailVerificationProducer;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional // что бы sent_at сохранилась в БД
    void sendMessages() {
        List<Outbox> pendingEvents = outboxRepository.findPendingEvents(LIMIT, OutboxStatus.PENDING.name());

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (Outbox event : pendingEvents) {
            try {
                EmailVerificationEvent emailSendEvent = mapToDto(event.getPayload());
                emailVerificationProducer.sendEmails(emailSendEvent);

                event.setStatus(OutboxStatus.DONE);
                event.setSentAt(Instant.now());
                log.info("Отправил сообщение {}", event);
            } catch (Exception e) {
                log.error("Ошибка собщения {}", event.getId(), e);

                event.setStatus(OutboxStatus.FAILED);
                event.setErrorReason(e.getMessage());
            }
        }
    }

    private EmailVerificationEvent mapToDto(String payload) {
        JsonNode node = objectMapper.readTree(payload);
        String code = node.get("code").asString();
        String email = node.get("email").asString();

        return new EmailVerificationEvent(email, code);
    }
}
