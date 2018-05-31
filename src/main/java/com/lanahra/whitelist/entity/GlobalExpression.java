package com.lanahra.whitelist.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * GlobalExpression
 * Whitelist of global expressions, regular expressions must be unique.
 */
@Entity
@Table(name = "global_whitelist",
uniqueConstraints = {@UniqueConstraint(columnNames = {"regex"})})
public class GlobalExpression extends Expression {

    public GlobalExpression() {
    }

    public GlobalExpression(Expression expression) {
        regex = expression.regex;
    }

    @Override
    public String toString() {
        return "GlobalExpression [regex=" + regex + "]";
    }
}
