package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath4")
public class TestRestFunction4 {
    @RestMethod(httpMethod = HttpMethod.POST, path = "/")
    public String test1() {
        return "test1";
    }

    @RestMethod(httpMethod = HttpMethod.GET, path = "/test")
    public String test2() {
        return "test2";
    }
}
