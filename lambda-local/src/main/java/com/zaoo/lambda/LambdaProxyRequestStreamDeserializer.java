package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectWriter;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LambdaProxyRequestStreamDeserializer implements LambdaStreamRequestDeserializer {
    private final LambdaProxyRequestDeserializer deserializer = new LambdaProxyRequestDeserializer();
    private final ObjectWriter objectWriter = ObjectMappers.getWriter();

    @Override
    public byte[] serialize(HttpServletRequest req) throws IOException {
        LambdaProxyRequest lambdaProxyRequest = deserializer.serialize(req);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            objectWriter.writeValue(out, lambdaProxyRequest);
            out.flush();
            return out.toByteArray();
        }
    }
}
