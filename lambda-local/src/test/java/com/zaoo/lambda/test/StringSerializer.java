package com.zaoo.lambda.test;

import com.zaoo.lambda.LambdaResponseSerializer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class StringSerializer implements LambdaResponseSerializer<String> {
    @Override
    public void deserialize(String output, HttpServletResponse resp) throws IOException {
        IOUtils.write(output, resp.getOutputStream(), "UTF-8");
    }
}
