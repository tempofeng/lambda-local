package com.zaoo.lambda.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaStreamRequestDeserializer;
import com.zaoo.lambda.LambdaStreamResponseSerializer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@SuppressWarnings("unused")
@LambdaLocal(value = "/helloStream", serializer = HelloStream.Serializer.class, deserializer = HelloStream.Deserializer.class)
public class HelloStream implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        String content = IOUtils.toString(input, "UTF-8");
        IOUtils.write(String.format("Hello %s!", content), output, "UTF-8");
    }

    public static class Deserializer implements LambdaStreamRequestDeserializer {
        @Override
        public byte[] serialize(HttpServletRequest req) throws IOException {
            return IOUtils.toByteArray(req.getInputStream());
        }
    }

    public static class Serializer implements LambdaStreamResponseSerializer {
        @Override
        public void deserialize(byte[] output, HttpServletResponse resp) throws IOException {
            IOUtils.write(output, resp.getOutputStream());
        }
    }
}
