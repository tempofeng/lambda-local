package com.zaoo.lambda.test;

import com.zaoo.lambda.LambdaRequestDeserializer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class StringDesearializer implements LambdaRequestDeserializer<String> {
    @Override
    public String serialize(HttpServletRequest req) throws IOException {
        return IOUtils.toString(req.getInputStream(), "UTF-8");
    }
}
