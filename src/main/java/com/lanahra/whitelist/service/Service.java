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

/**
 * Service
 * Responsible for persisting Expressions in the appropriate repository and
 * searching for a matching expression on validation, if any
 */
@Component
public class Service {

    private static final Logger LOGGER = LoggerFactory.getLogger(Service.class);

    private static final int PAGE_SIZE = 100000;

    @Autowired
    private GlobalWhitelistRepository globalWhitelistRepository;

    @Autowired
    private ClientWhitelistRepository clientWhitelistRepository;

    /**
     * Process Expression insertion in the repository
     * If client is null, then Expression is persisted in the Global Whitelist,
     * else, it is persisted in the Client Whitelist.
     *
     * @return Expression if successfuly persisted
     * @return null otherwise
     */
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

    /**
     * Process Expression validation
     * Given a request with a client and an URL, search through the repository
     * for a regular expression that matches the URL
     *
     * @return ValidationResponse
     *     match true if found a matching regular expression, false otherwise
     *     regex regular expression if found, null otherwise
     *     correlationId id for correlation with ValidationRequest
     */
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

    /**
     * Match URL
     * Search for an Expression that matches the URL, this is a single
     * concurrent unit of processing, looking through a single Page of
     * Expressions from either the Global or Client Whitelist, returns
     * CompletableFuture that can be resolved later.
     *
     * @return CompletableFuture with matching regular expression if found
     * @return CompletableFuture with null otherwise.
     */
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

    /**
     * Complete any not null future
     * Given a List of futures, returns the first one that is not null, in
     * other words, returns the first CompletableFuture with a matching regular
     * expression.
     */
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
