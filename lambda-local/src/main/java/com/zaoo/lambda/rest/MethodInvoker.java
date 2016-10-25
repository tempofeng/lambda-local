package com.zaoo.lambda.rest;

import com.google.common.base.Strings;
import com.zaoo.lambda.LambdaProxyRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class MethodInvoker {
    private static final Logger log = LoggerFactory.getLogger(MethodInvoker.class);
    private final Method method;
    private final String lambdaLocalPath;
    private final List<ParamRetriever> paramRetrievers = new ArrayList<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String methodPath;
    private final HttpMethod httpMethod;

    MethodInvoker(Method method, String lambdaLocalPath) {
        this.method = method;
        this.lambdaLocalPath = lambdaLocalPath;
        RestMethod restMethod = method.getAnnotation(RestMethod.class);
        methodPath = restMethod.path();
        httpMethod = restMethod.httpMethod();

        Parameter[] parameters = method.getParameters();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Annotation[] annotations = parameterAnnotations[i];
            Optional<Annotation> opt = Arrays.stream(annotations).filter(this::isRestAnnotation).findFirst();
            if (opt.isPresent()) {
                Annotation annotation = opt.get();
                RestParamDeserializer restParamDeserializer = createDeserializer(annotation);
                paramRetrievers.add(new ParamRetriever(ParamRetriever.ParamRetrieverType.ANNOTATION,
                        parameter,
                        annotation,
                        restParamDeserializer
                ));
            } else if (parameter.getType().isAssignableFrom(LambdaProxyRequest.class)) {
                paramRetrievers.add(new ParamRetriever(ParamRetriever.ParamRetrieverType.LAMBDA_PROXY_REQUEST,
                        parameter,
                        new ErrorAnnotation(),
                        new ErrorRestParamDeserializer()
                ));
            } else {
                throw new IllegalArgumentException(String.format("Parameter:%s in @RestMethod must be annotated",
                        parameter.getName()));
            }
        }
    }

    public String getMethodPath() {
        return methodPath;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    private boolean isRestAnnotation(Annotation annotation) {
        return annotation instanceof RestQuery ||
                annotation instanceof RestHeader ||
                annotation instanceof RestBody ||
                annotation instanceof RestForm ||
                annotation instanceof RestPath;
    }

    Object invoke(Object obj, LambdaProxyRequest request) throws InvocationTargetException {
        final Map<String, String> postParams = parsePostParameters(request);
        Map<String, String> pathVariables = pathMatcher.extractUriTemplateVariables(methodPath,
                getRestMethodPath(request));
        try {
            List<Object> args = paramRetrievers.stream()
                    .map(paramRetriever -> paramRetriever.retrieve(request, postParams, pathVariables))
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

        if (annotation instanceof RestPath) {
            RestPath restPath = (RestPath) annotation;
            return createDeserializer(restPath.deserializer());
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
                log.trace("MatchingMethodFound:methodPath={}", methodPath);
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

    static class ErrorRestParamDeserializer implements RestParamDeserializer<Object> {
        @Override
        public Object deserialize(String str, Class<?> cls) {
            throw new IllegalArgumentException("Unable to get RestParamDeserializer of this type:" + cls);
        }
    }

    static class ErrorAnnotation implements Annotation {
        @Override
        public Class<? extends Annotation> annotationType() {
            throw new IllegalArgumentException("Unable to get annotation type");
        }
    }
}
