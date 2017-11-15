package com.zaoo.lambda.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaoo.lambda.*;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractLambdaRestService extends AbstractLambdaLocalRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractLambdaRestService.class);
    private final List<MethodInvoker> methodInvokers;

    public AbstractLambdaRestService() {
        ObjectMappers.setObjectMapperFactory(getObjectMapperFactory());
        methodInvokers = createMethodInvokers(getClass());
    }

    protected ObjectMapperFactory getObjectMapperFactory() {
        return ObjectMapper::new;
    }

    @SuppressWarnings("unchecked")
    List<MethodInvoker> createMethodInvokers(Class<?> cls) {
        LambdaLocal lambdaLocal = cls.getAnnotation(LambdaLocal.class);
        if (lambdaLocal.value().length != 1) {
            throw new IllegalArgumentException(
                    "@LambdaLocal must have only one value() if using with AbstractLambdaRestService");
        }
        String lambdaLocalPath = lambdaLocal.value()[0];

        Set<Method> methods = ReflectionUtils.getMethods(cls, ReflectionUtils.withAnnotation(RestMethod.class));
        return methods.stream()
                .map(method -> new MethodInvoker(cls, method, lambdaLocalPath))
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

        // CORS pre-flight requests
        String accessControlRequestMethod = input.getHeaders().get("Access-Control-Request-Method");
        if (httpMethod == HttpMethod.OPTIONS && accessControlRequestMethod != null) {
            // Use AccessControlRequestMethod as HttpMethod
            input.setHttpMethod(accessControlRequestMethod.toUpperCase());
            for (MethodInvoker methodInvoker : methodInvokers) {
                if (methodInvoker.match(input)) {
                    return invokeCorsPreflightMethod(methodInvoker, input);
                }
            }
        }

        throw new IllegalArgumentException(String.format("Unhandled request:path=%s,method=%s",
                input.getPath(),
                httpMethod));
    }

    private LambdaProxyResponse invokeCorsPreflightMethod(MethodInvoker methodInvoker, LambdaProxyRequest input) {
        try {
            RestResponseEntity responseEntity = methodInvoker.invokeCorsPreflight(input);
            return new LambdaProxyResponse(responseEntity.getStatusCode(),
                    responseEntity.getHeaders(),
                    responseEntity.getBody(ObjectMappers.getWriter()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private LambdaProxyResponse invokeMethod(MethodInvoker methodInvoker, LambdaProxyRequest input) {
        try {
            log.debug("invokeMethod:methodPath={},httpMethod={}",
                    methodInvoker.getMethodPath(),
                    methodInvoker.getHttpMethod());
            RestResponseEntity responseEntity = methodInvoker.invoke(this, input);
            return new LambdaProxyResponse(responseEntity.getStatusCode(),
                    responseEntity.getHeaders(),
                    responseEntity.getBody(ObjectMappers.getWriter()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
