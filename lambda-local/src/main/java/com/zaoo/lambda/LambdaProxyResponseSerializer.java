package com.zaoo.lambda;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LambdaProxyResponseSerializer implements LambdaResponseSerializer<LambdaProxyResponse> {
    private static final String UTF_8 = "UTF-8";

    @Override
    public void deserialize(LambdaProxyResponse output, HttpServletResponse resp) throws IOException {
        IOUtils.write(output.getBody(), resp.getOutputStream(), UTF_8);
    }
}
