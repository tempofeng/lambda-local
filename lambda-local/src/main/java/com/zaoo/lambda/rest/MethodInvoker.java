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

import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

class MethodInvoker {
    private static final Logger log = LoggerFactory.getLogger(MethodInvoker.class);
    private final Method method;
    private final List<ParamRetriever> paramRetrievers = new ArrayList<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final String path;
    private final HttpMethod httpMethod;

    MethodInvoker(Method method) {
        this.method = method;
        RestMethod restMethod = method.getAnnotation(RestMethod.class);
        path = restMethod.path();
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

    private boolean isRestAnnotation(Annotation annotation) {
        return annotation instanceof RestQuery ||
                annotation instanceof RestHeader ||
                annotation instanceof RestBody ||
                annotation instanceof RestForm;
    }

    Object invoke(Object obj, LambdaProxyRequest request) throws InvocationTargetException {
        final Map<String, String> postParams = parsePostParameters(request);
        try {
            List<Object> args = paramRetrievers.stream()
                    .map(paramRetriever -> paramRetriever.retrieve(request, postParams))
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

    public boolean match(LambdaProxyRequest input) {
        log.info(String.format("path=%s,resource=%s", input, input.getResource()));
        String subPath = input.getPath().equals(input.getResource()) ?
                "/" :
                input.getPath().substring(input.getResource().length());
        if (pathMatcher.match(path, subPath)) {
            if (httpMethod == HttpMethod.ANY) {
                return true;
            }
            return httpMethod.equals(HttpMethod.valueOf(input.getHttpMethod().toUpperCase()));
        }
        return false;
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