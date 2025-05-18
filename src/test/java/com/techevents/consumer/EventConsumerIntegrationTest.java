package com.techevents.consumer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import com.techevents.config.KafkaTopics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;

@SpringBootTest(properties = {
    // H2 in-memory
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    // point at embedded broker
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    // fresh group + earliest offset
    "spring.kafka.consumer.group-id=event-integration-group",
    "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(partitions = 1, topics = { KafkaTopics.EVENTS_INGEST })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventConsumerIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerRegistry;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EventRepository repo;

    private ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() throws Exception {
        // ensure the listener is ready before sending
        for (MessageListenerContainer container : kafkaListenerRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        }
        // clear out any leftover events
        repo.deleteAll();
    }

    @Test
    void whenValidEvent_thenSavedToDatabase() throws Exception {
        Event e = new Event(
            "Integration Test",
            "desc",
            LocalDate.of(2025, 5, 18),
            "TestCity",
            List.of("tag1","tag2")
        );
        String payload = mapper.writeValueAsString(e);

        kafkaTemplate.send(KafkaTopics.EVENTS_INGEST, payload).get(5, TimeUnit.SECONDS);

        // give consumer a moment
        Thread.sleep(500);

        var all = repo.findAll();
        assertThat(all)
          .hasSize(1)
          .first()
          .extracting(Event::getTitle, Event::getDescription, Event::getCity)
          .containsExactly("Integration Test", "desc", "TestCity");
    }

    @Test
    void whenMissingFields_thenNothingSaved() throws Exception {
        String missing = """
            {
              "description":"no title or date",
              "location":"Nowhere",
              "tags":["x"]
            }
            """;

        kafkaTemplate.send(KafkaTopics.EVENTS_INGEST, missing).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);

        assertThat(repo.count()).isZero();
    }

    @Test
    void whenMalformedJson_thenNothingSaved() throws Exception {
        String bad = "{ not: 'json' ";

        kafkaTemplate.send(KafkaTopics.EVENTS_INGEST, bad).get(5, TimeUnit.SECONDS);
        Thread.sleep(500);

        assertThat(repo.count()).isZero();
    }
}
