package com.lanahra.whitelist.service;

import com.lanahra.whitelist.entity.Expression;
import javax.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class Listener {

    private static final String INSERTION_FACTORY = "insertionListenerContainerFactory";
    private static final String VALIDATION_FACTORY = "validationListenerContainerFactory";

    @Autowired
    private Service service;

    @Autowired
    private RabbitTemplate validationTemplate;

    @RabbitListener(queues = "${INSERTION_QUEUE}", containerFactory = INSERTION_FACTORY)
    public void listenInsertionQueue(@Valid @Payload Expression expression) {
        service.processExpressionInsertion(expression);
    }

    @RabbitListener(queues = "${VALIDATION_QUEUE}", containerFactory = VALIDATION_FACTORY)
    public void listenValidationQueue(@Valid @Payload ValidationRequest request) {
        ValidationResponse response = service.processExpressionValidation(request);
        validationTemplate.convertAndSend(response);
    }
}
