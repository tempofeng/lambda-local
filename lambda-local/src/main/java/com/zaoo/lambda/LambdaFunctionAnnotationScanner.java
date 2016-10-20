package com.zaoo.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class LambdaFunctionAnnotationScanner {
    public List<LambdaFunction> listLambdaFunctions(String packageName) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getTypesAnnotatedWith(LambdaLocal.class).stream()
                .flatMap(cls -> listLambdaFunctionFromAnotatedClass(cls).stream())
                .collect(Collectors.toList());
    }

    private LambdaRequestDeserializer createDeserializer(@Nullable Class<? extends LambdaRequestDeserializer<?>> cls) {
        if (cls == null) {
            return new LambdaProxyRequestDeserializer();
        }
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create:" + cls, e);
        }
    }

    private LambdaResponseSerializer createSerializer(@Nullable Class<? extends LambdaResponseSerializer<?>> cls) {
        if (cls == null) {
            return new LambdaProxyResponseSerializer();
        }
        try {
            return cls.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create:" + cls, e);
        }
    }

    private List<LambdaFunction> listLambdaFunctionFromAnotatedClass(Class<?> cls) {
        LambdaLocal lambdaLocal = cls.getAnnotation(LambdaLocal.class);
        String[] paths = lambdaLocal.value();
        String[] handlers = lambdaLocal.handler();
        Class<? extends LambdaRequestDeserializer<?>>[] deserializer = lambdaLocal.deserializer();
        Class<? extends LambdaResponseSerializer<?>>[] serializer = lambdaLocal.serializer();

        if (handlers.length == 0 & paths.length == 0) {
            throw new IllegalArgumentException(
                    "For class annotated with @LambdaLocal, there must be at least one path parameter:" + cls.getName());
        }

        if (handlers.length == 0 && paths.length == 1) {
            return Collections.singletonList(createLambdaFunction(paths[0],
                    cls.getName(),
                    Arrays.stream(deserializer).findFirst().orElse(null),
                    Arrays.stream(serializer).findFirst().orElse(null)));
        }

        if (handlers.length == 1 && paths.length == 1) {
            return Collections.singletonList(createLambdaFunction(paths[0],
                    handlers[0],
                    Arrays.stream(deserializer).findFirst().orElse(null),
                    Arrays.stream(serializer).findFirst().orElse(null)));
        }

        // more than one paths.
        if (handlers.length != paths.length) {
            throw new IllegalArgumentException(
                    "For class annotated with @LambdaLocal, the length of path array must be equal to the length of handler array:" + cls.getName());
        }

        if (handlers.length != deserializer.length) {
            throw new IllegalArgumentException(
                    "For class annotated with @LambdaLocal, the length of deserializer array must be equal to the length of handler array:" + cls.getName());
        }

        if (handlers.length != serializer.length) {
            throw new IllegalArgumentException(
                    "For class annotated with @LambdaLocal, the length of serializer array must be equal to the length of handler array:" + cls.getName());
        }

        List<LambdaFunction> lambdaFunctions = new ArrayList<>(paths.length);
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            String handler = handlers[i];
            Class<? extends LambdaRequestDeserializer<?>> deserializerClass = deserializer[i];
            Class<? extends LambdaResponseSerializer<?>> serializerClass = serializer[i];
            lambdaFunctions.add(createLambdaFunction(path, handler, deserializerClass, serializerClass));
        }
        return lambdaFunctions;
    }

    LambdaFunction createLambdaFunction(String path,
                                        String handler,
                                        Class<? extends LambdaRequestDeserializer<?>> deserializerClass,
                                        Class<? extends LambdaResponseSerializer<?>> serializerClass) {
        try {
            if (!handler.contains("::")) {
                Class<?> handlerClass = Class.forName(handler);
                if (ReflectionUtils.getAllSuperTypes(handlerClass, RequestHandler.class::equals).isEmpty()) {
                    throw new IllegalArgumentException("handler must implement RequestHandler or RequestStreamHandler:" + handler);
                }

                return new LambdaFunction(path,
                        handlerClass,
                        null,
                        createDeserializer(deserializerClass),
                        createSerializer(serializerClass));
            }

            String[] split = handler.split("::");
            String className = split[0];
            String methodName = split[1];

            Class<?> handlerClass = Class.forName(className);
            Set<Method> methods = ReflectionUtils.getMethods(handlerClass,
                    method -> findLambdaMethod(methodName, method));
            if (methods.size() == 0) {
                throw new IllegalArgumentException(
                        "Unable to find Lambda handler:" + handler);
            }
            if (methods.size() > 1) {
                throw new IllegalArgumentException(
                        "More than one method qualified for the handler:" + handler);
            }

            return new LambdaFunction(path,
                    handlerClass,
                    methods.stream().findFirst().get(),
                    createDeserializer(deserializerClass),
                    createSerializer(serializerClass));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to create new instance of Handler:" + handler, e);
        }
    }

    private boolean findLambdaMethod(String expectedMethodName, Method method) {
        if (!method.getName().equals(expectedMethodName)) {
            return false;
        }
        if (method.getParameterCount() == 1) {
            return true;
        }
        if (method.getParameterCount() == 2 && method.getParameterTypes()[1].equals(Context.class)) {
            return true;
        }
        return false;
    }
}
