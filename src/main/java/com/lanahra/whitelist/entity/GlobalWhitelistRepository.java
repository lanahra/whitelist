package com.lanahra.whitelist.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface GlobalWhitelistRepository extends CrudRepository<GlobalExpression, Long> {

    Page<Expression> findAll(Pageable pageable);
}
