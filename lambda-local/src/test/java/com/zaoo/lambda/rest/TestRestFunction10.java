package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaProxyRequest;

import java.util.Collections;

@LambdaLocal("/testRestPath10")
public class TestRestFunction10 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public String test1(@RestParam("test1") String test1, LambdaProxyRequest lambdaProxyRequest) {
        return lambdaProxyRequest.getPath();
    }
}
