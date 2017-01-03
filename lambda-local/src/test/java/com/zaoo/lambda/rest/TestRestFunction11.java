package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath11")
public class TestRestFunction11 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public String test1(@RestCookie("test1") String test1, @RestCookie(value = "test2", required = false) String test2) {
        return test1;
    }
}
