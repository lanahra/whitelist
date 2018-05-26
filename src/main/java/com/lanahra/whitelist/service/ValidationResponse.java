package com.lanahra.whitelist.service;

public class ValidationResponse {

    private Boolean match;
    private String regex;
    private Integer correlationId;

    public Boolean getMatch() {
        return match;
    }

    public void setMatch(Boolean match) {
        this.match = match;
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public Integer getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Integer correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "ValidationResponse [match="
            + match
            + ", regex="
            + regex
            + ", correlationId="
            + correlationId
            + "]";
    }
}
