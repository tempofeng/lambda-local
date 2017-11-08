package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.ObjectMappers;
import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;

class ParamRetriever {
    private static final Logger log = LoggerFactory.getLogger(ParamRetriever.class);
    private final ParamRetrieverType type;
    private final JavaType parameterJavaType;
    private final Annotation annotation;
    private final RestParamDeserializer<?> restParamDeserializer;
    private final ObjectReader objectReader = ObjectMappers.getReader();

    ParamRetriever(ParamRetrieverType type,
                   JavaType parameterJavaType,
                   Annotation annotation,
                   RestParamDeserializer<?> restParamDeserializer) {
        this.type = type;
        this.parameterJavaType = parameterJavaType;
        this.annotation = annotation;
        this.restParamDeserializer = restParamDeserializer;
    }

    Object retrieve(LambdaProxyRequest request, MethodInvocationContext context) {
        switch (type) {
            case ANNOTATION:
                return retrieveByAnnotation(context);
            case LAMBDA_PROXY_REQUEST:
                return request;
            default:
                throw new IllegalArgumentException("Unknown type:" + type);
        }
    }

    private Object retrieveByAnnotation(MethodInvocationContext context) {
        if (annotation instanceof RestParam) {
            RestParam restParam = (RestParam) annotation;
            String name = restParam.value();
            // Read from both post data and query string
            String valueStr = context.getQueryStringParameters().get(name);
            if (valueStr == null) {
                valueStr = context.getPostParams().get(name);
            }

            log.debug("getQueryParam:name={},value={}", name, valueStr);
            if (valueStr == null) {
                if (restParam.required()) {
                    throw new IllegalArgumentException(String.format("Request param:%s can't be null", name));
                } else {
                    return null;
                }
            }
            return restParamDeserializer.deserialize(valueStr, parameterJavaType);
        }

        if (annotation instanceof RestPath) {
            RestPath restPath = (RestPath) annotation;
            String name = restPath.value();
            String valueStr = context.getPathVariables().get(name);
            log.debug("getPathParam:name={},value={}", name, valueStr);
            if (valueStr == null) {
                if (restPath.required()) {
                    throw new IllegalArgumentException(String.format("Path param:%s can't be null", name));
                } else {
                    return null;
                }
            }
            return restParamDeserializer.deserialize(valueStr, parameterJavaType);
        }

        if (annotation instanceof RestBody) {
            RestBody restBody = (RestBody) annotation;
            String body = context.getBody();
            if (body == null || body.isEmpty()) {
                if (restBody.required()) {
                    throw new IllegalArgumentException("Request body can't be null");
                }
                return null;
            }

            try {
                return objectReader.forType(parameterJavaType).readValue(body);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (annotation instanceof RestHeader) {
            RestHeader restHeader = (RestHeader) annotation;
            String name = restHeader.value();
            String valueStr = context.getHeaders().get(name);
            log.debug("getHeaderParam:name={},value={}", name, valueStr);
            if (valueStr == null) {
                if (restHeader.required()) {
                    throw new IllegalArgumentException(String.format("Request header:%s can't be null", name));
                } else {
                    return null;
                }
            }
            return restParamDeserializer.deserialize(valueStr, parameterJavaType);
        }

        if (annotation instanceof RestCookie) {
            RestCookie restCookie = (RestCookie) annotation;
            String name = restCookie.value();
            Cookie cookie = context.getCookies().get(name);
            log.debug("getCookieParam:name={},value={}", name, cookie);
            if (cookie == null) {
                if (restCookie.required()) {
                    throw new IllegalArgumentException(String.format("Cookie:%s can't be null", name));
                } else {
                    return null;
                }
            }

            return restParamDeserializer.deserialize(cookie.getValue(), parameterJavaType);
        }

        throw new IllegalArgumentException("Unknown annotation:" + annotation);
    }

    enum ParamRetrieverType {
        ANNOTATION, LAMBDA_PROXY_REQUEST
    }
}
