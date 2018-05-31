package com.lanahra.whitelist.service;

import java.util.concurrent.Executor;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.validation.Validator;

/**
 * Configuration for RabbitMQ Listeners
 */
@Configuration
@EnableRabbit
@EnableAsync
public class ListenerConfiguration implements RabbitListenerConfigurer {

    @Autowired
    private Validator validator;

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Value("${INSERTION_QUEUE}")
    private String insertionQueueName;

    @Value("${VALIDATION_QUEUE}")
    private String validationQueueName;

    @Value("${RESPONSE_EXCHANGE}")
    private String validationExchangeName;

    @Value("${RESPONSE_ROUTING_KEY}")
    private String validationRoutingKey;

    @Value("${NUMBER_OF_VALIDATION_CONSUMERS}")
    private Integer numberValidationConsumers;

    /**
     * ListenerContainerFactory for insertion listener
     * Simple factory with standard settings.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory insertionListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(rabbitConnectionFactory);
        factory.setConcurrentConsumers(3);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAfterReceivePostProcessors(jsonPostProcessor());
        return factory;
    }

    /**
     * ListenerContainerFactory for validation listener
     * Concurrent consumers are defined by $NUMBER_OF_VALIDATION_CONSUMERS.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory validationListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(rabbitConnectionFactory);
        factory.setConcurrentConsumers(numberValidationConsumers);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAfterReceivePostProcessors(jsonPostProcessor());
        return factory;
    }

    /**
     * Listeners receive message in JSON format, after message is receive,
     * content type is changed to "application/json", in order for them to be
     * properly converted to JSON.
     */
    @Bean
    public MessagePostProcessor jsonPostProcessor() {
        return new MessagePostProcessor() {
            public Message postProcessMessage(Message message) {
                message.getMessageProperties().setContentType("application/json");
                return message;
            }
        };
    }

    /**
     * Set message message handler for Rabbit Listeners.
     */
    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }

    /**
     * Create message handler factory so incoming messages can be properly
     * validated.
     */
    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(validator);
        return factory;
    }

    /**
     * Create insertion queue in broker.
     */
    @Bean
    public Queue insertionQueue() {
        return new Queue(insertionQueueName);
    }

    /**
     * Create validation queue in broker.
     */
    @Bean
    public Queue validationQueue() {
        return new Queue(validationQueueName);
    }

    /**
     * Create validation exchange in broker
     */
    @Bean
    public Exchange validationExchange() {
        return new DirectExchange(validationExchangeName);
    }

    /**
     * Configure template for sending validation responses, with proper
     * exchange, routing key and JSON converter.
     */
    @Bean
    public RabbitTemplate validationTemplate() {
        RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
        template.setExchange(validationExchangeName);
        template.setRoutingKey(validationRoutingKey);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        return template;
    }
}
