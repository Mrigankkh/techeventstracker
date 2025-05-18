package com.techevents.service;

import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventIngestionServiceTest {

    private EventRepository repo;
    private RabbitTemplate rabbit;
    private EventIngestionService service;

    @BeforeEach
    void setUp() {
        repo = mock(EventRepository.class);
        rabbit = mock(RabbitTemplate.class);
        service = new EventIngestionService(repo, rabbit);

        // Inject config values manually since Spring isn't doing it
        ReflectionTestUtils.setField(service, "exchange", "test.exchange");
        ReflectionTestUtils.setField(service, "routingKey", "test.key");
    }

    @Test
    void missingTitle_shouldThrow() {
        Event e = new Event(null, "desc", LocalDate.now(), "city", List.of());
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.ingest(e));
        assertEquals("Event must have a title and an event date.", ex.getMessage());
        verifyNoInteractions(repo);
        verifyNoInteractions(rabbit);
    }

    @Test
    void validEvent_shouldPersistAndPublish() {
        Event e = new Event("Title", "desc", LocalDate.now(), "city", List.of());
        when(repo.save(any())).thenReturn(e);
        service.ingest(e);

        verify(repo).save(any());
        verify(rabbit).convertAndSend(anyString(), anyString(), eq(e));
    }

    @Test
    void dbError_shouldNotPublish() {
        Event e = new Event("Title", "desc", LocalDate.now(), "city", List.of());
        when(repo.save(any())).thenThrow(new RuntimeException("DB failure"));
        assertThrows(RuntimeException.class, () -> service.ingest(e));

        verify(repo).save(any());
        verify(rabbit, never())
                .convertAndSend(
                        ArgumentMatchers.<String>any(),
                        ArgumentMatchers.<String>any(),
                        ArgumentMatchers.<Object>any());
    }
}
