package com.techevents.consumer;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;


import com.techevents.config.KafkaTopics;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=password",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.EVENTS_INGEST})
public class EventConsumerTest {
     @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void testConsumerReceivesMessage() throws Exception {
        String testMessage = "{\"event\":\"ping\",\"user\":\"testUser\"}";

        kafkaTemplate.send(KafkaTopics.EVENTS_INGEST, testMessage);

        TimeUnit.SECONDS.sleep(2);

        System.out.println("Test message sent successfully");
    }
}
