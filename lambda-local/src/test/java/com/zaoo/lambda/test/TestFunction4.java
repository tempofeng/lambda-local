package com.zaoo.lambda.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import com.zaoo.lambda.ObjectMappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@LambdaLocal(value = "/testPath4")
public class TestFunction4 implements RequestStreamHandler {

    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        LambdaProxyRequest request = ObjectMappers.getReader().forType(LambdaProxyRequest.class).readValue(input);
        LambdaProxyResponse response = new LambdaProxyResponse(request.getBody());
        ObjectMappers.getWriter().writeValue(output, response);
    }
}
