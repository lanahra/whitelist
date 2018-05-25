package com.lanahra.whitelist.service;

public class ValidationMessage {

    private String client;
    private String url;
    private Integer correlationId;

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(Integer correlationId) {
        this.correlationId = correlationId;
    }

    @Override
    public String toString() {
        return "ValidationMessage [client="
            + client
            + ", url="
            + url
            + ", correlationId="
            + correlationId
            + "]";
    }
}
