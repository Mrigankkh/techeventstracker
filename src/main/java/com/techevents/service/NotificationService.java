package com.techevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techevents.model.NotificationMessage;
import com.techevents.model.Subscriber;
import com.techevents.dto.DailyEmail;
import com.techevents.model.Event;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.notification-exchange}")
    private String exchange;

    @Value("${app.rabbitmq.notification-routing-key}")
    private String routingKey;
    @Value("${app.rabbitmq.daily-summary-routing-key}")
    private String summaryRoutingKey;

    public NotificationService(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendNotification(Subscriber subscriber, Event event) {
        NotificationMessage message = new NotificationMessage(subscriber.getEmail(), event);

        try {
            String jsonPayload = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(exchange, routingKey, jsonPayload);
            log.info("Published notification for email={} to RabbitMQ", subscriber.getEmail());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize NotificationMessage for subscriber={}", subscriber.getEmail(), e);
        }
    }

    public void sendNotification(Subscriber subscriber, List<Event> events) {
        List<String> titles = events.stream()
                .map(Event::getTitle)
                .toList();

        DailyEmail summary = new DailyEmail(subscriber.getEmail(), titles);

        try {
            String payload = objectMapper.writeValueAsString(summary);
            rabbitTemplate.convertAndSend(exchange, summaryRoutingKey, payload);
            log.info("Published daily summary for {} to RabbitMQ", subscriber.getEmail());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize daily summary for {}", subscriber.getEmail(), e);
        }
    }
}
