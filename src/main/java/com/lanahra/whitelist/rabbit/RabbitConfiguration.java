package com.lanahra.whitelist.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfiguration {

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Value("${NUMBER_OF_VALIDATION_CONSUMERS}")
    private Integer numberValidationConsumers;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(rabbitConnectionFactory);
        factory.setConcurrentConsumers(numberValidationConsumers);
        factory.setMaxConcurrentConsumers(numberValidationConsumers);
        return factory;
    }

    @Value("${INSERTION_QUEUE}")
    private String insertionQueueName;

    @Bean
    public Queue insertionQueue() {
        return new Queue(insertionQueueName, true);
    }
}
