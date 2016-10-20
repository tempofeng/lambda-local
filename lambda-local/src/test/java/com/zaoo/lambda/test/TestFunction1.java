package com.zaoo.lambda.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaoo.lambda.LambdaLocal;

@LambdaLocal(value = "/testPath1")
public class TestFunction1 implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String input, Context context) {
        return "hello " + input;
    }
}
