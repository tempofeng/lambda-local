package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LambdaProxyResponseStreamSerializer implements LambdaStreamResponseSerializer {
    private static final String UTF_8 = "UTF-8";
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    @Override
    public void deserialize(byte[] output, HttpServletResponse resp) throws IOException {
        LambdaProxyResponse response = objectMapper.readValue(output, LambdaProxyResponse.class);
        resp.setStatus(response.getStatusCode());
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            resp.addHeader(entry.getKey(), entry.getValue());
        }
        IOUtils.write(response.getBody(), resp.getOutputStream(), UTF_8);
    }
}
