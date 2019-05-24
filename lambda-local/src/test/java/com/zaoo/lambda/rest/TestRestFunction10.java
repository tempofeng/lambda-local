package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaProxyRequest;

import java.util.Collections;

@LambdaLocal("/testRestPath10")
public class TestRestFunction10 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/test1")
    public String test1(@RestParam("test1") String test1, LambdaProxyRequest lambdaProxyRequest) {
        return test1 + "," + lambdaProxyRequest.getPath();
    }

    @RestMethod(httpMethod = HttpMethod.POST, path = "/test2")
    public String test2(@RestParam("test2") String test2, LambdaProxyRequest lambdaProxyRequest) {
        return test2 + "," + lambdaProxyRequest.getPath();
    }
}
