package com.techevents.consumer;


import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.techevents.config.KafkaTopics;

@Component
public class EventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @KafkaListener(topics = KafkaTopics.EVENTS_INGEST, groupId = "event-consumer-group")
    public void consume(String message) {
        logger.info("Received message: {}", message);
    }
}