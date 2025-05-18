package com.techevents.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventConsumerUnitTest {

    private EventRepository repo;
    private ObjectMapper mapper;
    private EventConsumer consumer;

    @BeforeEach
    void setUp() {
        repo = mock(EventRepository.class);
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        consumer = new EventConsumer(mapper, repo);
    }

    @Test
    void malformedJson_shouldNotSave_andLogError() {
        String badJson = "{ not: 'json' ";
        assertDoesNotThrow(() -> consumer.listen("t", 0, 0L, badJson));
        verify(repo, never()).save(any());
    }

    @Test
    void missingFields_shouldNotSave_andLogWarn() {
        String missing = """
            {
              "description":"no title/date",
              "city":"Nowhere"
            }
            """;
        assertDoesNotThrow(() -> consumer.listen("t", 0, 0L, missing));
        verify(repo, never()).save(any());
    }

    @Test
    void validJson_shouldSaveOnce() {
        String valid = """
            {
              "title":"UT Test",
              "description":"desc",
              "eventDate":"2025-05-18",
              "city":"Here",
              "tags":["a","b"]
            }
            """;
        consumer.listen("t", 0, 0L, valid);
        ArgumentCaptor<Event> cap = ArgumentCaptor.forClass(Event.class);
        verify(repo, times(1)).save(cap.capture());
        assertEquals("UT Test", cap.getValue().getTitle());
    }

    @Test
    void dbError_shouldPropagate_andLogError() {
        String valid = """
            {
              "title":"UT Test",
              "description":"desc",
              "eventDate":"2025-05-18",
              "city":"Here",
              "tags":["a","b"]
            }
            """;
        doThrow(new DataIntegrityViolationException("fk fail"))
            .when(repo).save(any());
        assertThrows(DataIntegrityViolationException.class,
                     () -> consumer.listen("t", 0, 0L, valid));
    }
}
