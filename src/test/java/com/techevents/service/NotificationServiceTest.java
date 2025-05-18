package com.techevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techevents.model.Event;
import com.techevents.model.Subscriber;
import com.techevents.model.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private RabbitTemplate rabbitTemplate;
    private ObjectMapper objectMapper;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        rabbitTemplate = mock(RabbitTemplate.class);
        objectMapper = new ObjectMapper();
        notificationService = new NotificationService(rabbitTemplate, objectMapper);

        // Inject config values manually
        ReflectionTestUtils.setField(notificationService, "exchange", "notifications.exchange");
        ReflectionTestUtils.setField(notificationService, "routingKey", "notifications.event.created");
    }

    @Test
    void sendNotification_shouldPublishToRabbitMQ_withCorrectPayload() {
        // Given
        Event event = new Event("Spring Boot Conf", "Great talk", LocalDate.of(2025, 6, 1), "Boston", List.of("spring"));
        Subscriber subscriber = new Subscriber("user@example.com");

        // When
        notificationService.sendNotification(subscriber, event);

        // Then
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq("notifications.exchange"), eq("notifications.event.created"), payloadCaptor.capture());

        String payload = payloadCaptor.getValue();
        assertTrue(payload.contains("user@example.com"));
        assertTrue(payload.contains("Spring Boot Conf"));
        assertTrue(payload.contains("2025-06-01"));
        assertTrue(payload.contains("Boston"));
    }
}
