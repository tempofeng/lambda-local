package com.zaoo.lambda;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LambdaProxyResponseSerializer implements LambdaResponseSerializer<LambdaProxyResponse> {
    @Override
    public void deserialize(LambdaProxyResponse output, HttpServletResponse resp) throws IOException {
        IOUtils.write(output.getBody(), resp.getOutputStream(), "UTF-8");
    }
}
