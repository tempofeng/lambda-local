package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.JavaType;

public interface RestParamDeserializer<T> {
    T deserialize(String str, JavaType javaType);
}
