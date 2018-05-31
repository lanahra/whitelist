package com.lanahra.whitelist.service;

import org.hibernate.validator.constraints.URL;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ValidationRequest {

    @NotNull
    @Size(min = 1, max = 128)
    private String client;

    @URL
    @NotNull
    @Size(min = 1, max = 128)
    private String url;

    @NotNull
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
        return "ValidationRequest [client="
            + client
            + ", url="
            + url
            + ", correlationId="
            + correlationId
            + "]";
    }
}
