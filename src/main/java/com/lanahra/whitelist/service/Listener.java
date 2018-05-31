package com.lanahra.whitelist.service;

import com.lanahra.whitelist.entity.Expression;
import javax.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Listener to retrieve RabbitMQ broker messages
 */
@Component
public class Listener {

    private static final String INSERTION_FACTORY = "insertionListenerContainerFactory";
    private static final String VALIDATION_FACTORY = "validationListenerContainerFactory";

    @Autowired
    private Service service;

    @Autowired
    private RabbitTemplate validationTemplate;

    /**
     * Receive incoming Expression insertion message, then process it with the
     * Service, incoming message is validated and must be of the JSON format:
     *
     * {"client": <string/nullable>, "regex": <string>}
     */
    @RabbitListener(queues = "${INSERTION_QUEUE}", containerFactory = INSERTION_FACTORY)
    public void listenInsertionQueue(@Valid @Payload Expression expression) {
        service.processExpressionInsertion(expression);
    }

    /**
     * Receive incoming URL validation request message, then process it with
     * the Service, incoming message is validated and must be of the JSON
     * format:
     *
     * {"client": <string>, "url": <string>, "correlationId": <integer>}
     *
     * Once the request is processed, the response is sent to the
     * $RESPONSE_EXCHANGE, the message is converted to the following JSON
     * format:
     *
     * {"match": <boolean>, "regex": <string/nullable>, "correlationId": <integer>}
     */
    @RabbitListener(queues = "${VALIDATION_QUEUE}", containerFactory = VALIDATION_FACTORY)
    public void listenValidationQueue(@Valid @Payload ValidationRequest request) {
        ValidationResponse response = service.processExpressionValidation(request);
        validationTemplate.convertAndSend(response);
    }
}
