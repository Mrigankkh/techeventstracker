package com.techevents.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import com.techevents.config.KafkaTopics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventRepository eventRepository;

    public EventConsumer(ObjectMapper objectMapper, EventRepository eventRepository) {
        this.objectMapper = objectMapper.registerModule(new JavaTimeModule());
        this.eventRepository = eventRepository;
    }

    @KafkaListener(
      topics = KafkaTopics.EVENTS_INGEST,
      groupId = "event-group",
      containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        @Payload String message
    ) {
        log.debug("Consuming message from {}-{}-{}", topic, partition, offset);

        // 1) DESERIALIZE
        Event event;
        try {
            event = objectMapper.readValue(message, Event.class);
            log.info("Parsed Event[id={}, title={}]", event.getId(), event.getTitle());
        } catch (JsonProcessingException e) {
            log.error("JSON parse error at {}-{}-{}; payload={}", topic, partition, offset, message, e);
            return;       
        }

        // 2) VALIDATE
        if (event.getTitle() == null || event.getEventDate() == null) {
            log.warn("Validation failed for {}-{}-{}; payload={}", topic, partition, offset, message);
            return;       
        }

        // 3) PERSIST (let DB errors bubble up)
        try {
            eventRepository.save(event);
            log.info("Saved Event[id={}, title={}]", event.getId(), event.getTitle());
        } catch (DataAccessException dbEx) {
            log.error("DB error saving Event[id={}, title={}] at {}-{}-{}", 
                      event.getId(), event.getTitle(), topic, partition, offset, dbEx);
            throw dbEx;   
        }
    }
}
