package com.lanahra.whitelist.service;

import com.lanahra.whitelist.entity.Expression;
import com.lanahra.whitelist.entity.ClientExpression;
import com.lanahra.whitelist.entity.ClientWhitelistRepository;
import com.lanahra.whitelist.entity.GlobalExpression;
import com.lanahra.whitelist.entity.GlobalWhitelistRepository;
import java.lang.Exception;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    private static final String INSERTION_FACTORY = "insertionListenerContainerFactory";
    private static final String VALIDATION_FACTORY = "validationListenerContainerFactory";

    private static final int PAGE_SIZE = 100000;

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

        long start = System.currentTimeMillis();

        List<CompletableFuture<String>> futures = new ArrayList<>();

        String client = request.getClient();
        String url = request.getUrl();

        Page<Expression> expressions;
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        do {
            expressions = globalWhitelistRepository.findAll(pageable);
            futures.add(matchExpressions(url, expressions));
            pageable = pageable.next();
        } while (expressions.hasNext());

        pageable = pageable.first();
        do {
            expressions = clientWhitelistRepository.findByClient(client, pageable);
            futures.add(matchExpressions(url, expressions));
            pageable = pageable.next();
        } while (expressions.hasNext());

        ValidationResponse response = new ValidationResponse();
        response.setCorrelationId(request.getCorrelationId());

        anyNotNull(futures)
            .thenAccept(regex -> {
                response.setMatch(true);
                response.setRegex(regex);
            })
            .whenComplete((ignored, t) -> {
                if (t != null) {
                    response.setMatch(false);
                }
            });

        LOGGER.info("Send " + response.toString());
        LOGGER.info("Elapsed time: " + (System.currentTimeMillis() - start));
        validationTemplate.convertAndSend(response);
    }

    @Async
    CompletableFuture<String> matchExpressions(String url, Page<Expression> expressions) {
        String response = null;

        for (Expression expression : expressions) {
            String regex = expression.getRegex();

            if (Pattern.matches(regex, url)) {
                response = regex;
                break;
            }
        }

        return CompletableFuture.completedFuture(response);
    }

    private static <T> CompletableFuture<T> anyNotNull(List<? extends CompletionStage<? extends T>> futures) {
        CompletableFuture<T> result = new CompletableFuture<>();

        Consumer<T> whenTrue = v -> {
            if (v != null) {
                result.complete(v);
            }
        };

        CompletableFuture.allOf(
                futures.stream()
                    .map(f -> f.thenAccept(whenTrue))
                    .toArray(CompletableFuture<?>[]::new)
        ).whenComplete((ignored, t) ->
            result.completeExceptionally(t != null ? t : new NoSuchElementException())
        );

        return result;
    }
}
