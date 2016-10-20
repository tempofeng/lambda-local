package com.zaoo.lambda.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaoo.lambda.LambdaLocal;

@LambdaLocal(value = "/testPath3",
        handler = "com.zaoo.lambda.test.TestFunction3",
        deserializer = StringDesearializer.class,
        serializer = StringSerializer.class)
public class TestFunction3 implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String input, Context context) {
        return input;
    }

}
