package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.ObjectMappers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;

class ParamRetriever {
    private final ParamRetrieverType type;
    private final Parameter parameter;
    private final Annotation annotation;
    private final RestParamDeserializer<?> restParamDeserializer;
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    ParamRetriever(ParamRetrieverType type,
                   Parameter parameter,
                   Annotation annotation,
                   RestParamDeserializer<?> restParamDeserializer) {
        this.type = type;
        this.parameter = parameter;
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
        if (annotation instanceof RestQuery) {
            RestQuery restQuery = (RestQuery) annotation;
            String name = restQuery.value();
            String valueStr = request.getQueryStringParameters().get(name);
            if (valueStr == null && restQuery.required()) {
                throw new IllegalArgumentException(String.format("Request param:%s can't be null", name));
            }
            return restParamDeserializer.deserialize(valueStr, parameter.getType());
        }

        if (annotation instanceof RestForm) {
            RestForm restForm = (RestForm) annotation;
            String name = restForm.value();
            String valueStr = postParams.get(name);
            if (valueStr == null && restForm.required()) {
                throw new IllegalArgumentException(String.format("Form param:%s can't be null", name));
            }
            return restParamDeserializer.deserialize(valueStr, parameter.getType());
        }

        if (annotation instanceof RestPath) {
            RestPath restPath = (RestPath) annotation;
            String name = restPath.value();
            String valueStr = pathVariables.get(name);
            if (valueStr == null && restPath.required()) {
                throw new IllegalArgumentException(String.format("Form param:%s can't be null", name));
            }
            return restParamDeserializer.deserialize(valueStr, parameter.getType());
        }

        if (annotation instanceof RestHeader) {
            RestHeader restHeader = (RestHeader) annotation;
            String name = restHeader.value();
            String valueStr = request.getHeaders().get(name);
            if (valueStr == null && restHeader.required()) {
                throw new IllegalArgumentException(String.format("Request header:%s can't be null", name));
            }
            return restParamDeserializer.deserialize(valueStr, parameter.getType());
        }

        if (annotation instanceof RestBody) {
            if (request.getBody() == null || request.getBody().isEmpty()) {
                return null;
            }

            try {
                return objectMapper.readValue(request.getBody(), parameter.getType());
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
