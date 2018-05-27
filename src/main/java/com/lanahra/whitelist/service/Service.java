package com.lanahra.whitelist.service;

import com.lanahra.whitelist.entity.Expression;
import com.lanahra.whitelist.entity.ClientExpression;
import com.lanahra.whitelist.entity.ClientWhitelistRepository;
import com.lanahra.whitelist.entity.GlobalExpression;
import com.lanahra.whitelist.entity.GlobalWhitelistRepository;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    private static final String INSERTION_FACTORY = "insertionListenerContainerFactory";
    private static final String VALIDATION_FACTORY = "validationListenerContainerFactory";

    @Autowired
    private GlobalWhitelistRepository globalWhitelistRepository;

    @Autowired
    private ClientWhitelistRepository clientWhitelistRepository;

    @Autowired
    private RabbitTemplate validationTemplate;

    @RabbitListener(queues = "${INSERTION_QUEUE}", containerFactory = INSERTION_FACTORY)
    public void listenInsertionQueue(@Valid @Payload Expression expression) {
        LOGGER.info("Receive " + expression.toString());

        try {
            if (expression.getClient() == null) {
                globalWhitelistRepository.save(new GlobalExpression(expression));
            } else {
                clientWhitelistRepository.save(new ClientExpression(expression));
            }
        } catch (DataIntegrityViolationException e) {
            LOGGER.info("Create Failed: " + e.getRootCause().toString());
        }
    }

    @RabbitListener(queues = "${VALIDATION_QUEUE}", containerFactory = VALIDATION_FACTORY)
    public void listenValidationQueue(@Valid @Payload ValidationRequest request) {
        LOGGER.info("Receive " + request.toString());

        ValidationResponse response = new ValidationResponse();
        response.setMatch(false);
        response.setCorrelationId(request.getCorrelationId());

        String client = request.getClient();

        List<Expression> expressions = new ArrayList<>();
        globalWhitelistRepository.findAll().forEach(expressions::add);
        clientWhitelistRepository.findByClient(client).forEach(expressions::add);

        for (Expression expression : expressions) {
            String regex = expression.getRegex();

            if (Pattern.matches(regex, request.getUrl())) {
                response.setMatch(true);
                response.setRegex(regex);
                break;
            }
        }

        LOGGER.info("Send " + response.toString());
        validationTemplate.convertAndSend(response);
    }
}
