package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ObjectMappers {
    private static ObjectMapper objectMapper;
    private static ObjectReader objectReader;
    private static ObjectWriter objectWriter;

    static {
        createInstance(ObjectMapper::new);
    }

    private static void createInstance(ObjectMapperFactory objectMapperFactory) {
        objectMapper = objectMapperFactory.createInstance();
        objectReader = objectMapper.reader();
        objectWriter = objectMapper.writer();
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static ObjectReader getReader() {
        return objectReader;
    }

    public static ObjectWriter getWriter() {
        return objectWriter;
    }

    public static void setObjectMapperFactory(ObjectMapperFactory objectMapperFactory) {
        createInstance(objectMapperFactory);
    }

    private ObjectMappers() {
    }
}
