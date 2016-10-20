package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LambdaProxyRequestStreamDeserializer implements LambdaStreamRequestDeserializer {
    private final LambdaProxyRequestDeserializer deserializer = new LambdaProxyRequestDeserializer();
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    @Override
    public byte[] serialize(HttpServletRequest req) throws IOException {
        LambdaProxyRequest lambdaProxyRequest = deserializer.serialize(req);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            objectMapper.writeValue(out, lambdaProxyRequest);
            out.flush();
            return out.toByteArray();
        }
    }
}
