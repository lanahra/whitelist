package com.lanahra.whitelist.entity;

import org.springframework.data.repository.CrudRepository;

public interface GlobalWhitelistRepository extends CrudRepository<GlobalExpression, Long> {
}
