package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class LambdaProxyResponseStreamSerializer implements LambdaStreamResponseSerializer {
    private static final String UTF_8 = "UTF-8";
    private final ObjectReader objectMapper = ObjectMappers.getReader();

    @Override
    public void deserialize(byte[] output, HttpServletResponse resp) throws IOException {
        LambdaProxyResponse response = objectMapper.forType(LambdaProxyResponse.class).readValue(output);
        resp.setStatus(response.getStatusCode());
        resp.setCharacterEncoding(UTF_8);
        for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
            resp.addHeader(entry.getKey(), entry.getValue());
        }
        IOUtils.write(response.getBody(), resp.getWriter());
        resp.getWriter().flush();
    }
}
