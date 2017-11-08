package com.zaoo.lambda.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Strings;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpec;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;

public class RestResponseEntity {
    private final int statusCode;
    @Nullable
    private final Object result;
    @Nullable
    private final String resultString;
    private final Map<String, String> headers;

    RestResponseEntity(int statusCode,
                       @Nullable Object result,
                       @Nullable String resultString,
                       Map<String, String> headers) {
        this.statusCode = statusCode;
        this.result = result;
        this.resultString = resultString;
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    @Nullable
    Object getResult() {
        return result;
    }

    @Nullable
    String getResultString() {
        return resultString;
    }

    public String getBody(ObjectWriter objectWriter) throws JsonProcessingException {
        if (resultString != null) {
            return resultString;
        }
        return objectWriter.writeValueAsString(result);
    }

    public static class Builder {
        private int statusCode = 200;
        private Object result;
        private String resultString;
        private Map<String, String> headers = new HashMap<>();
        private List<Cookie> cookies = new ArrayList<>();

        public Builder withStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder withResult(Object result) {
            this.result = result;
            if (Strings.isNullOrEmpty(headers.get("Content-Type"))) {
                headers.put("Content-Type", "application/json");
            }
            return this;
        }

        public Builder withResultString(String resultString) {
            this.resultString = resultString;
            if (Strings.isNullOrEmpty(headers.get("Content-Type"))) {
                headers.put("Content-Type", "text/html");
            }
            return this;
        }

        public Builder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder addHeaders(Map<String, String> h) {
            headers.putAll(h);
            return this;
        }

        public Builder addCookie(String name, String value) {
            cookies.add(new BasicClientCookie(name, value));
            return this;
        }

        public Builder addCookie(String name, String value, Instant expires, String path, String domain) {
            BasicClientCookie clientCookie = new BasicClientCookie(name, value);
            clientCookie.setExpiryDate(Date.from(expires));
            clientCookie.setPath(path);
            clientCookie.setDomain(domain);
            cookies.add(clientCookie);
            return this;
        }

        public RestResponseEntity build() {
            if (resultString == null && result == null) {
                throw new IllegalArgumentException("Either resultString or result must be set");
            }

            if (!cookies.isEmpty()) {
                DefaultCookieSpec defaultCookieSpec = new DefaultCookieSpec();
                defaultCookieSpec.formatCookies(cookies)
                        .forEach(header -> headers.put(header.getName(), header.getValue()));
            }
            return new RestResponseEntity(statusCode, result, resultString, headers);
        }
    }
}
