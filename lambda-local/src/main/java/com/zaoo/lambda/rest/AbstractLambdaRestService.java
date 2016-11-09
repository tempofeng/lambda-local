package com.zaoo.lambda.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.*;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractLambdaRestService extends AbstractLambdaLocalRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractLambdaRestService.class);
    private final List<MethodInvoker> methodInvokers;
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    public AbstractLambdaRestService() {
        methodInvokers = createMethodInvokers(getClass());
    }

    List<MethodInvoker> createMethodInvokers(Class<?> cls) {
        LambdaLocal lambdaLocal = cls.getAnnotation(LambdaLocal.class);
        if (lambdaLocal.value().length != 1) {
            throw new IllegalArgumentException(
                    "@LambdaLocal must have only one value() if using with AbstractLambdaRestService");
        }
        String lambdaLocalPath = lambdaLocal.value()[0];

        return ReflectionUtils.getMethods(cls, ReflectionUtils.withAnnotation(RestMethod.class)).stream()
                .map(method -> new MethodInvoker(method, lambdaLocalPath))
                .collect(Collectors.toList());
    }

    @Override
    public LambdaProxyResponse handleRequest(LambdaProxyRequest input, Context context) {
        HttpMethod httpMethod = HttpMethod.valueOf(input.getHttpMethod().toUpperCase());
        for (MethodInvoker methodInvoker : methodInvokers) {
            if (methodInvoker.match(input)) {
                return invokeMethod(methodInvoker, input);
            }
        }
        throw new IllegalArgumentException(String.format("Unhandled request:path=%s,method=%s",
                input.getPath(),
                httpMethod));
    }

    private LambdaProxyResponse invokeMethod(MethodInvoker methodInvoker, LambdaProxyRequest input) {
        try {
            log.debug("invokeMethod:methodPath={},httpMethod={}",
                    methodInvoker.getMethodPath(),
                    methodInvoker.getHttpMethod());
            MethodInvoker.Result result = methodInvoker.invoke(this, input);
            return new LambdaProxyResponse(result.statusCode,
                    result.headers,
                    objectMapper.writeValueAsString(result.result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            log.error(e.getLocalizedMessage(), e);
            try {
                Error error;
                if (e.getCause() != null) {
                    error = new Error(e.getCause().getLocalizedMessage(), e.getCause());
                } else {
                    error = new Error(e.getLocalizedMessage(), e);
                }
                return new LambdaProxyResponse(500, Collections.emptyMap(), objectMapper.writeValueAsString(error));
            } catch (JsonProcessingException jpe) {
                throw new RuntimeException(jpe);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            try {
                return new LambdaProxyResponse(500,
                        Collections.emptyMap(),
                        objectMapper.writeValueAsString(new Error(e.getLocalizedMessage(), e)));
            } catch (JsonProcessingException jpe) {
                throw new RuntimeException(jpe);
            }
        }
    }

    public static class Error {
        private String message;
        @JsonProperty(value = "exception")
        private String exceptionClass;

        Error(String message, Throwable t) {
            this.message = message;
            this.exceptionClass = t.getClass().getName();
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getExceptionClass() {
            return exceptionClass;
        }

        public void setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
        }
    }

}
