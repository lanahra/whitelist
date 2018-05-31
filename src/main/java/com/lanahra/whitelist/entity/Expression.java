package com.lanahra.whitelist.entity;

import com.lanahra.whitelist.validation.ValidPattern;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Expression superclass
 *
 * This class is used to validate incoming messages to the service, the
 * incoming message must have this format and be valid under the fields
 * constraints, no table is created for Expression, it must be further
 * specialized into either ClientExpression, if the client field is not null,
 * or GlobalExpression otherwise.
 *
 * @see ClientExpression
 * @see GlobalExpression
 */
@MappedSuperclass
public class Expression {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Size(min = 1, max = 128)
    protected String client;

    @NotNull
    @ValidPattern
    @Size(min = 1, max = 128)
    protected String regex;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Override
    public String toString() {
        return "Expression [client="
            + client
            + ", regex="
            + regex
            + "]";
    }
}
