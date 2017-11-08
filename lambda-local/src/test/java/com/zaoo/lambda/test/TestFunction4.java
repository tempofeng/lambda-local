package com.zaoo.lambda.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import com.zaoo.lambda.ObjectMappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@LambdaLocal(value = "/testPath4")
public class TestFunction4 implements RequestStreamHandler {
    private final ObjectReader objectReader = ObjectMappers.getInstance();
    private final ObjectWriter objectWriter = ObjectMappers.getWriter();

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaProxyRequest request = objectReader.readValue(input);
        LambdaProxyResponse response = new LambdaProxyResponse(request.getBody());
        objectWriter.writeValue(output, response);
    }
}
