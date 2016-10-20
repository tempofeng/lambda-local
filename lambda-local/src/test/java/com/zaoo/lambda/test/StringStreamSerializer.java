package com.zaoo.lambda.test;

import com.zaoo.lambda.LambdaStreamResponseSerializer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StringStreamSerializer implements LambdaStreamResponseSerializer {
    @Override
    public void deserialize(byte[] output, HttpServletResponse resp) throws IOException {
        IOUtils.write(output, resp.getOutputStream());
    }
}
