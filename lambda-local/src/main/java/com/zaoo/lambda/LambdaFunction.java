package com.zaoo.lambda;

import java.lang.reflect.Method;
import java.util.Objects;

class LambdaFunction {
    private final String path;
    private final Class handlerClass;
    private final Method handlerMethod;
    private LambdaRequestDeserializer deserializer;
    private LambdaResponseSerializer serializer;

    public LambdaFunction(String path,
                          Class handlerClass,
                          Method handlerMethod,
                          LambdaRequestDeserializer deserializer,
                          LambdaResponseSerializer serializer) {
        this.path = path;
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
        this.deserializer = deserializer;
        this.serializer = serializer;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Method getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LambdaFunction that = (LambdaFunction) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(handlerClass, that.handlerClass) &&
                Objects.equals(handlerMethod, that.handlerMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, handlerClass, handlerMethod);
    }

    public LambdaRequestDeserializer getDeserializer() {
        return deserializer;
    }

    public LambdaResponseSerializer getSerializer() {
        return serializer;
    }

    @Override
    public String toString() {
        return "LambdaFunction{" +
                "path='" + path + '\'' +
                ", handlerClass=" + handlerClass +
                ", handlerMethod=" + handlerMethod +
                '}';
    }
}
