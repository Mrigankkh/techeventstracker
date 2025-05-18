package com.techevents.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    public void testValidJsonDeserialization() throws JsonProcessingException {
        String json = """
        {
          "title": "Tech Conference",
          "description": "Annual tech meetup",
          "eventDate": "2025-06-01",
          "city": "San Francisco",
          "tags": ["conference", "dev"]
        }
        """;

        Event event = objectMapper.readValue(json, Event.class);

        assertEquals("Tech Conference", event.getTitle());
        assertEquals(LocalDate.of(2025, 6, 1), event.getEventDate());
        assertEquals("San Francisco", event.getCity());
        assertEquals(List.of("conference", "dev"), event.getTags());
    }

    @Test
    public void testInvalidJsonDeserialization() {
        String invalidJson = "{ title: \"oops\" ";

        assertThrows(JsonProcessingException.class, () -> {
            objectMapper.readValue(invalidJson, Event.class);
        });
    }
}
