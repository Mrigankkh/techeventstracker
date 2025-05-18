package com.techevents.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techevents.model.Event;
import com.techevents.service.EventIngestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EventConsumerUnitTest {

    private ObjectMapper mapper;
    private EventIngestionService ingestionService;
    private EventConsumer consumer;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ingestionService = mock(EventIngestionService.class);
        consumer = new EventConsumer(mapper, ingestionService);
    }

    @Test
    void malformedJson_shouldNotCallService() {
        String badJson = "{ this is not valid JSON";
        consumer.listen("topic1", badJson);
        verifyNoInteractions(ingestionService);
    }

    @Test
    void missingRequiredFields_shouldNotCallService() {
        String missingFieldsJson = """
            {
              "description": "missing title and date",
              "city": "X"
            }
        """;
        consumer.listen("topic1", missingFieldsJson);
        verifyNoInteractions(ingestionService);
    }

    @Test
    void validJson_shouldCallServiceWithEvent() {
        String validJson = """
            {
              "title": "Test Title",
              "description": "desc",
              "eventDate": "2025-06-01",
              "city": "Test City",
              "tags": ["tag1", "tag2"]
            }
        """;
        consumer.listen("topic1", validJson);
        verify(ingestionService, times(1)).ingest(any(Event.class));
    }

    @Test
    void serviceThrowsException_shouldNotCrashConsumer() {
        String validJson = """
            {
              "title": "Test Title",
              "description": "desc",
              "eventDate": "2025-06-01",
              "city": "Test City",
              "tags": ["tag1", "tag2"]
            }
        """;
        doThrow(new RuntimeException("Simulated failure"))
            .when(ingestionService).ingest(any());
        assertDoesNotThrow(() -> consumer.listen("topic1", validJson));
    }
}
