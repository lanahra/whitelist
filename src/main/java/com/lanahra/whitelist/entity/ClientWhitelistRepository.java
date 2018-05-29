package com.lanahra.whitelist.entity;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ClientWhitelistRepository extends CrudRepository<ClientExpression, Long> {

    Page<Expression> findByClient(String client, Pageable pageable);
}
