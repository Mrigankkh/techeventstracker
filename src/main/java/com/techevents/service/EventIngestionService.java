package com.techevents.service;

import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final EventRepository eventRepository;
    private final RabbitTemplate rabbit;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    public EventIngestionService(EventRepository eventRepository, RabbitTemplate rabbitTemplate) {
        this.eventRepository = eventRepository;
        this.rabbit = rabbitTemplate;
    }

    public void ingest(Event event) {
        // Validate
        if (event.getTitle() == null || event.getEventDate() == null) {
            throw new IllegalArgumentException("Event must have a title and an event date.");
        }

        // 1) Persist to database
        Event saved = eventRepository.save(event);
        log.info("Saved Event[id={}, title={}]", saved.getId(), saved.getTitle());

        // 2) Publish to RabbitMQ
        rabbit.convertAndSend(exchange, routingKey, saved);
        log.info("Published Event[id={}] to RabbitMQ [exchange={}, routingKey={}]", 
                 saved.getId(), exchange, routingKey);
    }
}
