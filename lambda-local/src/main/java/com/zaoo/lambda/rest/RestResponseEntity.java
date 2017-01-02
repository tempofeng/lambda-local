package com.zaoo.lambda.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RestResponseEntity {
    private int statusCode;
    private Object result;
    private Map<String, String> headers;

    public RestResponseEntity() {
        headers = new HashMap<>();
    }

    public RestResponseEntity(int statusCode, Object result, Map<String, String> headers) {
        this.statusCode = statusCode;
        this.result = result;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestResponseEntity that = (RestResponseEntity) o;
        return statusCode == that.statusCode &&
                Objects.equals(result, that.result) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusCode, result, headers);
    }
}
