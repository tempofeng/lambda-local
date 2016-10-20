package com.zaoo.lambda;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LambdaProxyResponseStreamSerializer implements LambdaStreamResponseSerializer {
    @Override
    public void deserialize(byte[] output, HttpServletResponse resp) throws IOException {
        IOUtils.write(output, resp.getOutputStream());
    }
}
