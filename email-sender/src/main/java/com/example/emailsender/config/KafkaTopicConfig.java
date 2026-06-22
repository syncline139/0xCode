package com.example.emailsender.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic verificationCodeTopic() {
        return TopicBuilder
                .name("verification-code-topic")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
