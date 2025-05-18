package com.techevents.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
  classes = RabbitMQConfig.class,
  properties = {
    "app.rabbitmq.queue=test.queue",
    "app.rabbitmq.exchange=test.exchange",
    "app.rabbitmq.routing-key=test.key"
  }
)
@ImportAutoConfiguration(RabbitAutoConfiguration.class)
class RabbitMQConfigTest {

  @Autowired Queue eventQueue;
  @Autowired TopicExchange eventExchange;
  @Autowired Binding eventBinding;
  @Autowired RabbitTemplate rabbitTemplate;

  @Test void queueIsConfigured() {
    assertThat(eventQueue.getName()).isEqualTo("test.queue");
    assertThat(eventQueue.isDurable()).isTrue();
  }

  @Test void exchangeIsConfigured() {
    assertThat(eventExchange.getName()).isEqualTo("test.exchange");
    assertThat(eventExchange.isDurable()).isTrue();
  }

  @Test void bindingIsConfigured() {
    assertThat(eventBinding.getExchange()).isEqualTo(eventExchange.getName());
    assertThat(eventBinding.getDestination()).isEqualTo(eventQueue.getName());
    assertThat(eventBinding.getRoutingKey()).isEqualTo("test.key");
  }

  @Test void rabbitTemplateIsAvailable() {
    assertThat(rabbitTemplate).isNotNull();
  }
}
