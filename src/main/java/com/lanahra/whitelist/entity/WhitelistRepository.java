package com.lanahra.whitelist.entity;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface WhitelistRepository extends CrudRepository<Expression, Long> {

    List<Expression> findExpressionByClient(String client);
}
