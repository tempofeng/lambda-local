package com.zaoo.lambda;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LambdaProxyRequestStreamDeserializer implements LambdaStreamRequestDeserializer {
    private final LambdaProxyRequestDeserializer deserializer = new LambdaProxyRequestDeserializer();

    @Override
    public byte[] serialize(HttpServletRequest req) throws IOException {
        LambdaProxyRequest lambdaProxyRequest = deserializer.serialize(req);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ObjectMappers.getWriter().writeValue(out, lambdaProxyRequest);
            out.flush();
            return out.toByteArray();
        }
    }
}
