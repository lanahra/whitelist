package com.lanahra.whitelist.entity;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Whitelist with the client Expressions
 */
public interface ClientWhitelistRepository extends CrudRepository<ClientExpression, Long> {

    /**
     * Find all the Expressions in a Page of a particular client
     */
    Page<Expression> findByClient(String client, Pageable pageable);
}
