package com.lanahra.whitelist.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

/**
 * Whitelist with the global Expressions
 */
public interface GlobalWhitelistRepository extends CrudRepository<GlobalExpression, Long> {

    /**
     * Retrieve all Expressions within a Page
     */
    Page<Expression> findAll(Pageable pageable);
}
