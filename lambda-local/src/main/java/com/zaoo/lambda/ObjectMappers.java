package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ObjectMappers {
    private static final ObjectReader OBJECT_READER;
    private static final ObjectWriter OBJECT_WRITER;

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        OBJECT_READER = objectMapper.reader();
        OBJECT_WRITER = objectMapper.writer();
    }

    public static ObjectReader getInstance() {
        return OBJECT_READER;
    }

    public static ObjectWriter getWriter() {
        return OBJECT_WRITER;
    }

    private ObjectMappers() {
    }
}
