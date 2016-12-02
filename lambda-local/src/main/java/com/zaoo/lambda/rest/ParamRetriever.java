package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Map;

class ParamRetriever {
    private static final Logger log = LoggerFactory.getLogger(ParamRetriever.class);
    private final ParamRetrieverType type;
    private final JavaType parameterJavaType;
    private final Annotation annotation;
    private final RestParamDeserializer<?> restParamDeserializer;
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    ParamRetriever(ParamRetrieverType type,
                   JavaType parameterJavaType,
                   Annotation annotation,
                   RestParamDeserializer<?> restParamDeserializer) {
        this.type = type;
        this.parameterJavaType = parameterJavaType;
        this.annotation = annotation;
        this.restParamDeserializer = restParamDeserializer;
    }

    Object retrieve(LambdaProxyRequest request, Map<String, String> postParams, Map<String, String> pathVariables) {
        switch (type) {
            case ANNOTATION:
                return retrieveByAnnotation(request, postParams, pathVariables);
            case LAMBDA_PROXY_REQUEST:
                return request;
            default:
                throw new IllegalArgumentException("Unknown type:" + type);
        }
    }

    private Object retrieveByAnnotation(LambdaProxyRequest request,
                                        Map<String, String> postParams,
                                        Map<String, String> pathVariables) {
        log.debug("retrieveByAnnotation:post={},path={}", postParams, pathVariables);

        if (annotation instanceof RestParam) {
            RestParam restParam = (RestParam) annotation;
            String name = restParam.value();
            // Read from both post data and query string
            String valueStr = postParams.get(name);
            if (valueStr == null) {
                valueStr = request.getQueryStringParameters().get(name);
            }

            log.debug("getQueryParam:annotation={},name={},value={}", annotation, name, valueStr);
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
            String valueStr = pathVariables.get(name);
            log.debug("getPathParam:annotation={},name={},value={}", annotation, name, valueStr);
            if (valueStr == null) {
                if (restPath.required()) {
                    throw new IllegalArgumentException(String.format("Path param:%s can't be null", name));
                } else {
                    return null;
                }
            }
            return restParamDeserializer.deserialize(valueStr, parameterJavaType);
        }

        if (annotation instanceof RestHeader) {
            RestHeader restHeader = (RestHeader) annotation;
            String name = restHeader.value();
            String valueStr = request.getHeaders().get(name);
            log.debug("getHeaderParam:annotation={},name={},value={}", annotation, name, valueStr);
            if (valueStr == null) {
                if (restHeader.required()) {
                    throw new IllegalArgumentException(String.format("Request header:%s can't be null", name));
                } else {
                    return null;
                }
            }
            return restParamDeserializer.deserialize(valueStr, parameterJavaType);
        }

        if (annotation instanceof RestBody) {
            RestBody restBody = (RestBody) annotation;
            if (request.getBody() == null || request.getBody().isEmpty()) {
                if (restBody.required()) {
                    throw new IllegalArgumentException(String.format("Request body can't be null"));
                }
                return null;
            }

            try {
                return objectMapper.readValue(request.getBody(), parameterJavaType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        throw new IllegalArgumentException("Unknown annotation:" + annotation);
    }

    enum ParamRetrieverType {
        ANNOTATION, LAMBDA_PROXY_REQUEST
    }
}
