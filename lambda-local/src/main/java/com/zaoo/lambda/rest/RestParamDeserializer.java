package com.zaoo.lambda.rest;

public interface RestParamDeserializer<T> {
    T deserialize(String str, Class<?> cls);
}
