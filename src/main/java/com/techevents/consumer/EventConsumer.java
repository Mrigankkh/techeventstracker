package com.techevents.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.techevents.config.KafkaTopics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    private final ObjectMapper objectMapper;
    private final EventRepository eventRepository;

    public EventConsumer(ObjectMapper objectMapper, EventRepository eventRepository) {
        this.objectMapper = objectMapper;
        this.objectMapper.registerModule(new JavaTimeModule());
        this.eventRepository = eventRepository;
    }

    @KafkaListener(topics = KafkaTopics.EVENTS_INGEST, groupId = "event-group")
   public void listen(String message) {
        try {
            Event event = objectMapper.readValue(message, Event.class);

            log.info("Received Event: {}", event);

            if (event.getTitle() == null || event.getEventDate() == null) {
                log.warn("Invalid event payload: {}", message);
                return;
            }

            eventRepository.save(event);
            log.info("Saved event: {}", event.getTitle());

        } catch (JsonProcessingException e) {
            log.error("Failed to parse message: {}", message, e);
        }
    }
}