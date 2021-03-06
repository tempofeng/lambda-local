package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

import java.util.Collections;

@LambdaLocal("/testRestPath9")
public class TestRestFunction9 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public RestResponseEntity test1() {
        return new RestResponseEntity.Builder()
                .withResult("Hello")
                .addHeader("testName", "testValue")
                .addCookie("testCookieName", "testCookieValue")
                .build();
    }
}
