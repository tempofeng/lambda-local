package com.zaoo.lambda.rest;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.zaoo.lambda.AbstractLambdaLocalRequestHandler;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import com.zaoo.lambda.ObjectMappers;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.reflections.ReflectionUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class AbstractLambdaRestService extends AbstractLambdaLocalRequestHandler {
    private final Map<HttpMethod, MethodInvoker> methodInvokers;
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    public AbstractLambdaRestService() {
        methodInvokers = ReflectionUtils.getMethods(getClass(),
                ReflectionUtils.withAnnotation(RestMethod.class)).stream()
                .collect(Collectors.toMap(
                        method -> method.getAnnotation(RestMethod.class).value(),
                        method -> new MethodInvoker(objectMapper, method)));
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

    static class MethodInvoker {
        private final Method method;
        private final List<ArgRetriever> argRetrievers = new ArrayList<>();

        MethodInvoker(ObjectMapper objectMapper, Method method) {
            this.method = method;

            Parameter[] parameters = method.getParameters();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                Annotation[] annotations = parameterAnnotations[i];
                Optional<Annotation> opt = Arrays.stream(annotations).filter(this::isRestAnnotation).findFirst();
                if (opt.isPresent()) {
                    Annotation annotation = opt.get();
                    RestParamDeserializer restParamDeserializer = createDeserializer(annotation);
                    argRetrievers.add(new ArgRetriever(objectMapper, ArgRetrieverType.ANNOTATION,
                            parameter,
                            annotation,
                            restParamDeserializer
                    ));
                } else if (parameter.getType().isAssignableFrom(LambdaProxyRequest.class)) {
                    argRetrievers.add(new ArgRetriever(objectMapper, ArgRetrieverType.LAMBDA_PROXY_REQUEST,
                            parameter,
                            null,
                            null
                    ));
                } else {
                    throw new IllegalArgumentException(String.format("Parameter:%s in @RestMethod must be annotated",
                            parameter.getName()));
                }
            }
        }

        private boolean isRestAnnotation(Annotation annotation) {
            return annotation instanceof RestQuery ||
                    annotation instanceof RestHeader ||
                    annotation instanceof RestBody ||
                    annotation instanceof RestForm;
        }

        Object invoke(Object obj, LambdaProxyRequest request) throws InvocationTargetException {
            final Map<String, String> postParams = parsePostParameters(request);
            try {
                List<Object> args = argRetrievers.stream()
                        .map(argRetriever -> argRetriever.retrieve(request, postParams))
                        .collect(toList());
                return method.invoke(obj, args.toArray());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, String> parsePostParameters(LambdaProxyRequest request) {
            if (!"application/x-www-form-urlencoded".equals(request.getHeaders().get("Content-Type"))) {
                return Collections.emptyMap();
            }

            if (Strings.isNullOrEmpty(request.getBody())) {
                return Collections.emptyMap();
            }

            return URLEncodedUtils.parse(request.getBody(), Charset.forName("UTF-8")).stream()
                    .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        }

        private RestParamDeserializer createDeserializer(Annotation annotation) {
            if (annotation instanceof RestQuery) {
                RestQuery restQuery = (RestQuery) annotation;
                return createDeserializer(restQuery.deserializer());
            }

            if (annotation instanceof RestForm) {
                RestForm restForm = (RestForm) annotation;
                return createDeserializer(restForm.deserializer());
            }

            if (annotation instanceof RestHeader) {
                RestHeader restHeader = (RestHeader) annotation;
                return createDeserializer(restHeader.deserializer());
            }

            return new ErrorRestParamDeserializer();
        }

        private RestParamDeserializer createDeserializer(Class<? extends RestParamDeserializer<?>> deserializer) {
            try {
                return deserializer.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ErrorRestParamDeserializer implements RestParamDeserializer<Object> {
        @Override
        public Object deserialize(String str, Class<?> cls) {
            throw new IllegalArgumentException("Unable to get RestParamDeserializer of this type:" + cls);
        }
    }

    private static class ArgRetriever {
        private final ArgRetrieverType type;
        private final Parameter parameter;
        private final Annotation annotation;
        private final ObjectMapper objectMapper;
        private final RestParamDeserializer<?> restParamDeserializer;

        ArgRetriever(ObjectMapper objectMapper,
                     ArgRetrieverType type,
                     Parameter parameter,
                     @Nullable Annotation annotation,
                     RestParamDeserializer<?> restParamDeserializer) {
            this.type = type;
            this.parameter = parameter;
            this.annotation = annotation;
            this.objectMapper = objectMapper;
            this.restParamDeserializer = restParamDeserializer;
        }

        Object retrieve(LambdaProxyRequest request, Map<String, String> postParams) {
            switch (type) {
                case ANNOTATION:
                    if (annotation instanceof RestForm) {
                        RestForm restForm = (RestForm) annotation;
                        String name = restForm.value();
                        String valueStr = postParams.get(name);
                        if (valueStr == null && restForm.required()) {
                            throw new IllegalArgumentException(String.format("Form param:%s can't be null", name));
                        }
                        return restParamDeserializer.deserialize(valueStr, parameter.getType());
                    }

                    if (annotation instanceof RestQuery) {
                        RestQuery restQuery = (RestQuery) annotation;
                        String name = restQuery.value();
                        String valueStr = request.getQueryStringParameters().get(name);
                        if (valueStr == null && restQuery.required()) {
                            throw new IllegalArgumentException(String.format("Request param:%s can't be null", name));
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
                case LAMBDA_PROXY_REQUEST:
                    return request;
                default:
                    throw new IllegalArgumentException("Unknown type:" + type);
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

    private enum ArgRetrieverType {
        ANNOTATION, LAMBDA_PROXY_REQUEST
    }
}
