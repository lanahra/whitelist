package com.lanahra.whitelist.service;

import com.lanahra.whitelist.entity.Expression;
import com.lanahra.whitelist.entity.ClientExpression;
import com.lanahra.whitelist.entity.ClientWhitelistRepository;
import com.lanahra.whitelist.entity.GlobalExpression;
import com.lanahra.whitelist.entity.GlobalWhitelistRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    private static final int PAGE_SIZE = 100000;

    @Autowired
    private GlobalWhitelistRepository globalWhitelistRepository;

    @Autowired
    private ClientWhitelistRepository clientWhitelistRepository;

    public Expression processExpressionInsertion(Expression expression) {
        LOGGER.info("Process " + expression.toString());

        Expression save = null;

        try {
            if (expression.getClient() == null) {
                save = globalWhitelistRepository.save(new GlobalExpression(expression));
            } else {
                save = clientWhitelistRepository.save(new ClientExpression(expression));
            }
        } catch (DataIntegrityViolationException e) {
            LOGGER.info("Create Failed: " + e.getMessage());
        }

        return save;
    }

    public ValidationResponse processExpressionValidation(ValidationRequest request) {
        LOGGER.info("Process " + request.toString());

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

        LOGGER.info("Elapsed time: " + (System.currentTimeMillis() - start));
        LOGGER.info("Return " + response.toString());

        return response;
    }

    @Async
    private CompletableFuture<String> matchExpressions(String url, Page<Expression> expressions) {
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
