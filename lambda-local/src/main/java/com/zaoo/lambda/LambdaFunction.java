package com.zaoo.lambda;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Objects;

class LambdaFunction {
    private final String path;
    private final Class handlerClass;
    private final Method handlerMethod;
    private final LambdaRequestDeserializer deserializer;
    private final LambdaResponseSerializer serializer;

    public LambdaFunction(String path,
                          Class handlerClass,
                          @Nullable Method handlerMethod,
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

    /**
     * @return null if the handler is a RequestHandler class.
     */
    @Nullable
    public Method getHandlerMethod() {
        return handlerMethod;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
