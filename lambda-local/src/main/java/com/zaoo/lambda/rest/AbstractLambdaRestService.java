package com.zaoo.lambda.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaoo.lambda.AbstractLambdaLocalRequestHandler;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import com.zaoo.lambda.ObjectMappers;
import org.reflections.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractLambdaRestService extends AbstractLambdaLocalRequestHandler {
    private final Map<HttpMethod, MethodInvoker> methodInvokers;
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    public AbstractLambdaRestService() {
        methodInvokers = createMethodInvokers(getClass());
    }

    Map<HttpMethod, MethodInvoker> createMethodInvokers(Class<?> cls) {
        return ReflectionUtils.getMethods(cls,
                ReflectionUtils.withAnnotation(RestMethod.class)).stream()
                .collect(Collectors.toMap(
                        method -> method.getAnnotation(RestMethod.class).value(),
                        MethodInvoker::new));
    }

    @Override
    public LambdaProxyResponse handleRequest(LambdaProxyRequest input, Context context) {
        HttpMethod httpMethod = HttpMethod.valueOf(input.getHttpMethod().toUpperCase());
        MethodInvoker methodInvoker = methodInvokers.get(httpMethod);
        if (methodInvoker == null) {
            methodInvoker = methodInvokers.get(HttpMethod.ANY);
        }
        if (methodInvoker == null) {
            throw new IllegalArgumentException("Unable to find implementation of HTTP method:" + httpMethod);
        }

        try {
            Object result = methodInvoker.invoke(this, input);
            return new LambdaProxyResponse(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            try {
                Error error = new Error(e.getLocalizedMessage(), e);
                return new LambdaProxyResponse(500, Collections.emptyMap(), objectMapper.writeValueAsString(error));
            } catch (JsonProcessingException jpe) {
                throw new RuntimeException(jpe);
            }
        }
    }

    static class Error {
        public String message;
        @JsonProperty(value = "exception")
        public String exceptionClass;

        Error(String message, Throwable t) {
            this.message = message;
            this.exceptionClass = t.getClass().getName();
        }
    }

}
