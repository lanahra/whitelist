package com.lanahra.whitelist.service;

import com.lanahra.whitelist.entity.Expression;
import com.lanahra.whitelist.entity.GlobalExpression;
import com.lanahra.whitelist.entity.GlobalWhitelistRepository;
import com.lanahra.whitelist.entity.WhitelistRepository;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import javax.validation.Valid;

@Component
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    @Autowired
    private GlobalWhitelistRepository globalWhitelistRepository;

    @Autowired
    private WhitelistRepository whitelistRepository;

    @RabbitListener(queues = "${INSERTION_QUEUE}")
    public void listenInsertionQueue(@Valid @Payload Expression expression) {
        LOGGER.info("Received " + expression.toString());

        try {
            Pattern.compile(expression.getRegex());

            if (expression.getClient() == null) {
                GlobalExpression globalExpression = new GlobalExpression();
                globalExpression.setRegex(expression.getRegex());

                globalWhitelistRepository.save(globalExpression);
            } else {
                whitelistRepository.save(expression);
            }
        } catch (PatternSyntaxException e) {
            LOGGER.info("Bad RegEx: " + e.toString());
        } catch (DataIntegrityViolationException e) {
            LOGGER.info("Create Failed: " + e.getRootCause().toString());
        }
    }

    @RabbitListener(queues = "${VALIDATION_QUEUE}")
    public void listenValidationQueue(ValidationMessage message) {
        LOGGER.info("Received " + message.toString());
    }
}
