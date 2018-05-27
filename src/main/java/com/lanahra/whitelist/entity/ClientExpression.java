package com.lanahra.whitelist.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "client_whitelist",
uniqueConstraints = {@UniqueConstraint(columnNames = {"client", "regex"})})
public class ClientExpression extends Expression {

    public ClientExpression() {
    }

    public ClientExpression(Expression expression) {
        client = expression.client;
        regex = expression.regex;
    }

    @Override
    public String toString() {
        return "ClientExpression [client=" + client + ", regex=" + regex + "]";
    }
}
