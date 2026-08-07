package com.example.hotelbooking.Configs;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic newTopic() {

        return TopicBuilder.name("booking-topic").
                partitions(2).
                replicas(1).
                build();
    }
}
