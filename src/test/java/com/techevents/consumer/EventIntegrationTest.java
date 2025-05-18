package com.techevents.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techevents.model.Event;
import com.techevents.repository.EventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import com.techevents.config.KafkaTopics;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=event-integration-group",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = { KafkaTopics.EVENTS_INGEST })
public class EventIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @BeforeEach
    void setup() {
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        }
    }

    @Test
    public void testKafkaToDatabaseFlow() throws Exception {
        Event testEvent = new Event(
                "Kafka Test",
                "This is a test event",
                LocalDate.now(),
                "Boston",
                List.of("test", "kafka"));

        String json = objectMapper.writeValueAsString(testEvent);

        kafkaTemplate.send(KafkaTopics.EVENTS_INGEST, json).get();

        int retry = 0;
        List<Event> events;
        do {
            TimeUnit.SECONDS.sleep(1); 
            events = eventRepository.findAll();
            retry++;
        } while (events.isEmpty() && retry < 5);

        assertFalse(events.isEmpty(), "Expected event to be saved in DB");
        assertEquals("Kafka Test", events.get(0).getTitle());
    }
}
