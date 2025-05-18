package com.techevents.consumer;

import com.techevents.config.KafkaTopics;
import com.techevents.model.Event;
import com.techevents.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=password",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=pipeline-test-group",
        "spring.kafka.consumer.auto-offset-reset=earliest"
    }
)
@EmbeddedKafka(partitions = 1, topics = { KafkaTopics.EVENTS_INGEST })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventPipelineIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private EventRepository repository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private org.springframework.kafka.config.KafkaListenerEndpointRegistry kafkaListenerRegistry;

    @Value("${local.server.port}")
    private int port;

    private Event testEvent;

    @BeforeEach
    void setUp() throws Exception {
        // Ensure the Kafka listener has been assigned before sending
        for (MessageListenerContainer container : kafkaListenerRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
        }
        // clean out any events left over
        repository.deleteAll();

        testEvent = new Event(
            "E2E Test",
            "Full pipeline",
            LocalDate.of(2025, 5, 18),
            "TestCity",
            List.of("e2e", "kafka")
        );
    }

    @Test
    void endToEnd_ingestPersistAndExpose_viaRest() throws Exception {
        // 1) send to Kafka
        String payload = new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .writeValueAsString(testEvent);

        kafkaTemplate.send(KafkaTopics.EVENTS_INGEST, payload).get(5, TimeUnit.SECONDS);

        // 2) wait for DB write
        List<Event> saved;
        int attempts = 0;
        do {
            TimeUnit.MILLISECONDS.sleep(200);
            saved = repository.findAll();
            attempts++;
        } while (saved.isEmpty() && attempts < 20);

        assertThat(saved)
            .hasSize(1)
            .first()
            .extracting(Event::getTitle, Event::getDescription, Event::getCity)
            .containsExactly(
                testEvent.getTitle(),
                testEvent.getDescription(),
                testEvent.getCity()
            );

        // 3) call REST endpoint
        String base = "http://localhost:" + port + "/events";
        ResponseEntity<Event[]> response = restTemplate.getForEntity(base, Event[].class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(1);

        Event returned = response.getBody()[0];
        assertThat(returned.getTitle()).isEqualTo(testEvent.getTitle());
        assertThat(returned.getDescription()).isEqualTo(testEvent.getDescription());
        assertThat(returned.getEventDate()).isEqualTo(testEvent.getEventDate());
        assertThat(returned.getCity()).isEqualTo(testEvent.getCity());
        assertThat(returned.getTags()).containsExactlyElementsOf(testEvent.getTags());
    }
}
