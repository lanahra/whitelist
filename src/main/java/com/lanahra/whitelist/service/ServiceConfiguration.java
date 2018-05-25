package com.lanahra.whitelist.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.validation.Validator;

@Configuration
@EnableRabbit
public class ServiceConfiguration implements RabbitListenerConfigurer {

    @Autowired
    private Validator validator;

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Value("${INSERTION_QUEUE}")
    private String insertionQueueName;

    @Value("${VALIDATION_QUEUE}")
    private String validationQueueName;

    @Value("${NUMBER_OF_VALIDATION_CONSUMERS}")
    private Integer numberValidationConsumers;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(rabbitConnectionFactory);
        factory.setConcurrentConsumers(numberValidationConsumers);
        factory.setMaxConcurrentConsumers(numberValidationConsumers);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());

        factory.setAfterReceivePostProcessors(new MessagePostProcessor() {
            public Message postProcessMessage(Message message) {
                message.getMessageProperties().setContentType("application/json");
                return message;
            }
        });

        return factory;
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(validator);
        return factory;
    }

    @Bean
    public Queue insertionQueue() {
        return new Queue(insertionQueueName, true);
    }

    @Bean
    public Queue validationQueue() {
        return new Queue(validationQueueName, true);
    }
}
