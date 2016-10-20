package com.zaoo.lambda.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.zaoo.lambda.LambdaProxyRequestDeserializer;
import com.zaoo.lambda.LambdaProxyResponseSerializer;
import com.zaoo.lambda.LambdaLocal;

@LambdaLocal(value = {"/testPath2", "/testPath3"},
        handler = {"com.zaoo.lambda.test.TestFunction2::method1", "com.zaoo.lambda.test.TestFunction2::method2"},
        deserializer = {LambdaProxyRequestDeserializer.class, LambdaProxyRequestDeserializer.class},
        serializer = {LambdaProxyResponseSerializer.class, LambdaProxyResponseSerializer.class})
public class TestFunction2 {
    public String method1(String input) {
        return "hello " + input;
    }

    public String method2(String input, Context context) {
        return "hi " + input;
    }
}
