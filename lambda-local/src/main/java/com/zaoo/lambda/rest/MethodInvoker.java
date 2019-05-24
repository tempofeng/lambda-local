package com.zaoo.lambda.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

import static java.util.stream.Collectors.toList;

class MethodInvoker {
    private static final Logger log = LoggerFactory.getLogger(MethodInvoker.class);
    private final Method method;
    private final String lambdaLocalPath;
    private final List<ParamRetriever> paramRetrievers = new ArrayList<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String methodPath;
    private final HttpMethod httpMethod;
    private final Map<String, String> headers = new HashMap<>();

    public MethodInvoker(Class<?> cls, Method method, String lambdaLocalPath) {
        log.debug("addMethodInvoker:cls={},method={},path={}", cls.getSimpleName(), method.getName(), lambdaLocalPath);

        this.method = method;
        this.lambdaLocalPath = lambdaLocalPath;

        CrossOrigin crossOrigin = method.getAnnotation(CrossOrigin.class);
        if (crossOrigin != null) {
            headers.putAll(createCrossOriginHeaders(crossOrigin));
        } else {
            CrossOrigin classCrossOrigin = cls.getAnnotation(CrossOrigin.class);
            if (classCrossOrigin != null) {
                headers.putAll(createCrossOriginHeaders(classCrossOrigin));
            }
        }

        CacheControl cacheControl = method.getAnnotation(CacheControl.class);
        if (cacheControl != null) {
            headers.put("Cache-Control", cacheControl.value());
        } else {
            CacheControl classCacheControl = cls.getAnnotation(CacheControl.class);
            if (classCacheControl != null) {
                headers.put("Cache-Control", classCacheControl.value());
            }
        }

        RestMethod restMethod = method.getAnnotation(RestMethod.class);
        methodPath = restMethod.path();
        httpMethod = restMethod.httpMethod();

        Parameter[] parameters = method.getParameters();
        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            JavaType javaType = ObjectMappers.getReader().getTypeFactory().constructType(parameterTypes[i]);
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> opt = Arrays.stream(annotations).filter(this::isRestAnnotation).findFirst();
            if (opt.isPresent()) {
                Annotation annotation = opt.get();
                log.debug("addParam:annotation={},type={}",
                        annotation.annotationType().getSimpleName(),
                        javaType.getTypeName());
                RestParamDeserializer restParamDeserializer = createDeserializer(annotation);
                paramRetrievers.add(new ParamRetriever(ParamRetriever.ParamRetrieverType.ANNOTATION,
                        javaType,
                        annotation,
                        restParamDeserializer
                ));
            } else if (parameter.getType().isAssignableFrom(LambdaProxyRequest.class)) {
                log.debug("addLambdaProxyRequest:type={}", javaType.getTypeName());
                paramRetrievers.add(new ParamRetriever(ParamRetriever.ParamRetrieverType.LAMBDA_PROXY_REQUEST,
                        javaType,
                        new ErrorAnnotation(),
                        new ErrorRestParamDeserializer()
                ));
            } else {
                throw new IllegalArgumentException(String.format("Parameter:%s in @RestMethod must be annotated",
                        parameter.getName()));
            }
        }
    }

    private Map<String, String> createCrossOriginHeaders(CrossOrigin classCrossOrigin) {
        return ImmutableMap.of("Access-Control-Allow-Origin", classCrossOrigin.value(),
                "Access-Control-Allow-Methods", classCrossOrigin.allowMethods(),
                "Access-Control-Allow-Headers", classCrossOrigin.allowedHeaders());
    }

    public String getMethodPath() {
        return methodPath;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    private boolean isRestAnnotation(Annotation annotation) {
        return annotation instanceof RestParam ||
                annotation instanceof RestHeader ||
                annotation instanceof RestCookie ||
                annotation instanceof RestBody ||
                annotation instanceof RestPath;
    }

    RestResponseEntity invoke(Object obj, LambdaProxyRequest request) {
        MethodInvocationContext context = new MethodInvocationContext(lambdaLocalPath, methodPath, request);
        try {
            List<Object> args = paramRetrievers.stream()
                    .map(paramRetriever -> paramRetriever.retrieve(request, context))
                    .collect(toList());
            Object result = method.invoke(obj, args.toArray());

            if (result instanceof RestResponseEntity) {
                return (RestResponseEntity) result;
            }

            return new RestResponseEntity.Builder()
                    .withResult(result != null ? result : "")
                    .addHeaders(getCrossOriginHeaders(request))
                    .build();
        } catch (InvocationTargetException e) {
            Error error;
            if (e.getCause() != null) {
                log.error(e.getCause().getLocalizedMessage(), e.getCause());
                error = new Error(e.getCause().getLocalizedMessage(), e.getCause());
            } else {
                log.error(e.getLocalizedMessage(), e);
                error = new Error(e.getLocalizedMessage(), e);
            }

            return new RestResponseEntity.Builder()
                    .withStatusCode(500)
                    .withResult(error)
                    .addHeaders(getCrossOriginHeaders(request))
                    .build();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            Error error = new Error(e.getLocalizedMessage(), e);
            return new RestResponseEntity.Builder()
                    .withStatusCode(500)
                    .withResult(error)
                    .addHeaders(getCrossOriginHeaders(request))
                    .build();
        }
    }

    private Map<String, String> getCrossOriginHeaders(LambdaProxyRequest request) {
        if ("*".equals(headers.get("Access-Control-Allow-Headers"))) {
            // Access-Control-Request-Headers can't be '*'
            // http://stackoverflow.com/questions/13146892/cors-access-control-allow-headers-wildcard-being-ignored
            String accessControlRequestHeaders = request.getHeaders().get("Access-Control-Request-Headers") != null ?
                    request.getHeaders().get("Access-Control-Request-Headers") :
                    request.getHeaders().get("access-control-request-headers");
            if (!Strings.isNullOrEmpty(accessControlRequestHeaders)) {
                HashMap<String, String> newHeaders = new HashMap<>(headers);
                newHeaders.put("Access-Control-Allow-Headers", accessControlRequestHeaders);
                return Collections.unmodifiableMap(newHeaders);
            }
        }
        return headers;
    }

    RestResponseEntity invokeCorsPreflight(LambdaProxyRequest request) {
        return new RestResponseEntity(200, Collections.emptyMap(), null, getCrossOriginHeaders(request));
    }

    private RestParamDeserializer createDeserializer(Annotation annotation) {
        if (annotation instanceof RestParam) {
            RestParam restParam = (RestParam) annotation;
            return createDeserializer(restParam.deserializer());
        }

        if (annotation instanceof RestPath) {
            RestPath restPath = (RestPath) annotation;
            return createDeserializer(restPath.deserializer());
        }

        if (annotation instanceof RestHeader) {
            RestHeader restHeader = (RestHeader) annotation;
            return createDeserializer(restHeader.deserializer());
        }

        if (annotation instanceof RestCookie) {
            RestCookie restCookie = (RestCookie) annotation;
            return createDeserializer(restCookie.deserializer());
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

    public boolean match(LambdaProxyRequest input) {
        String restMethodPath = getRestMethodPath(input);
        log.trace("findingMatchingMethod:methodPath={},requestPath={},restMethodPath={},resource={}",
                methodPath,
                input.getPath(),
                restMethodPath,
                input.getResource());
        if (pathMatcher.match(methodPath, restMethodPath)) {
            if (httpMethod == HttpMethod.ANY ||
                    httpMethod.equals(HttpMethod.valueOf(input.getHttpMethod().toUpperCase()))) {
                log.trace("matchedMethodFound:methodPath={}", methodPath);
                return true;
            }
        }
        return false;
    }

    private String getRestMethodPath(LambdaProxyRequest input) {
        return input.getPath().equals(lambdaLocalPath) ?
                "/" :
                input.getPath().substring(lambdaLocalPath.length());
    }

    private static class ErrorRestParamDeserializer implements RestParamDeserializer<Object> {
        @Override
        public Object deserialize(String str, JavaType javaType) {
            throw new IllegalArgumentException("Unable to get RestParamDeserializer of this type:" + javaType);
        }
    }

    private static class ErrorAnnotation implements Annotation {
        @Override
        public Class<? extends Annotation> annotationType() {
            throw new IllegalArgumentException("Unable to get annotation type");
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
