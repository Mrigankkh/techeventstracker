package com.techevents.repository;

import com.techevents.model.Event;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void shouldSaveAndRetrieveEvent() {
        Event event = new Event(
            "Spring Boot Workshop",
            "Learn Spring Boot with hands-on examples",
            LocalDate.of(2025, 6, 1),
            "Berlin",
            List.of("java", "spring", "bootcamp")
        );

        eventRepository.save(event);

        List<Event> events = eventRepository.findAll();

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTitle()).isEqualTo("Spring Boot Workshop");
    }
}
