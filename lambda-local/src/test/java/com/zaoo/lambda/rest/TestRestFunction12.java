package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath12")
@CacheControl
public class TestRestFunction12 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public String test1() {
        return "Hello";
    }
}
