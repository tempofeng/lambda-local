package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperFactory {
    ObjectMapper createInstance();
}
