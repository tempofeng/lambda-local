package com.zaoo.lambda.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import org.reflections.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractLambdaRestService implements RequestHandler<LambdaProxyRequest, LambdaProxyResponse> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<HttpMethod, MethodInvoker> methodInvokers;

    public AbstractLambdaRestService() {
        methodInvokers = ReflectionUtils.getMethods(getClass(),
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

    private static class MethodInvoker {
        private Method method;
        private List<ArgRetriever> argRetrievers = new ArrayList<>();

        public MethodInvoker(Method method) {
            this.method = method;

            Parameter[] parameters = method.getParameters();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Annotation[] annotations = parameterAnnotations[i];
                Optional<Annotation> opt = Arrays.stream(annotations).filter(this::isRestAnnotation).findFirst();
                if (opt.isPresent()) {
                    argRetrievers.add(new ArgRetriever(ArgRetrieverType.ANNOTATION, parameter, opt.get()));
                } else if (parameter.getType().isAssignableFrom(LambdaProxyRequest.class)) {
                    argRetrievers.add(new ArgRetriever(ArgRetrieverType.LAMBDA_PROXY_REQUEST, parameter, null));
                } else {
                    throw new IllegalArgumentException(String.format("Parameter:%s in @RestMethod must be annotated",
                            parameter.getName()));
                }
            }
        }

        private boolean isRestAnnotation(Annotation annotation) {
            return annotation instanceof RestQuery || annotation instanceof RestHeader || annotation instanceof RestBody;
        }

        public Object invoke(Object obj, LambdaProxyRequest request) throws InvocationTargetException {
            try {
                List<Object> args = argRetrievers.stream().map(argRetriever -> argRetriever.retrieve(request)).collect(
                        Collectors.toList());
                return method.invoke(obj, args.toArray());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ArgRetriever {
        private ArgRetrieverType type;
        private Parameter parameter;
        private Annotation annotation;

        public ArgRetriever(ArgRetrieverType type, Parameter parameter, Annotation annotation) {
            this.type = type;
            this.parameter = parameter;
            this.annotation = annotation;
        }

        public Object retrieve(LambdaProxyRequest request) {
            switch (type) {
                case ANNOTATION:
                    if (annotation instanceof RestQuery) {
                        RestQuery restQuery = (RestQuery) annotation;
                        String name = restQuery.value();
                        String value = request.getQueryStringParameters().get(name);
                        if (value == null && restQuery.required()) {
                            throw new IllegalArgumentException(String.format("Request param:%s can't be null", name));
                        }
                        return value;
                    }

                    if (annotation instanceof RestHeader) {
                        RestHeader restHeader = (RestHeader) annotation;
                        String name = restHeader.value();
                        String value = request.getHeaders().get(name);
                        if (value == null && restHeader.required()) {
                            throw new IllegalArgumentException(String.format("Request header:%s can't be null", name));
                        }
                        return value;
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
                case LAMBDA_PROXY_REQUEST:
                    return request;
                default:
                    throw new IllegalArgumentException("Unknown type:" + type);
            }
        }
    }

    public static class Error {
        public String message;
        @JsonProperty(value = "exception")
        public String exceptionClass;

        public Error(String message, Throwable t) {
            this.message = message;
            this.exceptionClass = t.getClass().getName();
        }
    }

    private enum ArgRetrieverType {
        ANNOTATION, LAMBDA_PROXY_REQUEST
    }
}
