package com.zaoo.lambda.test;

import com.zaoo.lambda.LambdaStreamRequestDeserializer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class StringStreamDesearializer implements LambdaStreamRequestDeserializer {
    @Override
    public byte[] serialize(HttpServletRequest req) throws IOException {
        return IOUtils.toByteArray(req.getInputStream());
    }
}
