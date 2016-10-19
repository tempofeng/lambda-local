package com.zaoo.lambda;

import java.util.Collections;
import java.util.Map;

public class LambdaProxyResponse {
    private int statusCode;
    private Map<String, String> headers;
    private String body;

    public LambdaProxyResponse() {
    }

    public LambdaProxyResponse(int statusCode, Map<String, String> headers, String body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public LambdaProxyResponse(String body) {
        this(200, Collections.emptyMap(), body);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
