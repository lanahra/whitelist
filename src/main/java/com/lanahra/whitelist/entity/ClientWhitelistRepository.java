package com.lanahra.whitelist.entity;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface ClientWhitelistRepository extends CrudRepository<ClientExpression, Long> {

    List<ClientExpression> findByClient(String client);
}
