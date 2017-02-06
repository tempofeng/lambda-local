package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath13")
@CacheControl
public class TestRestFunction13 {
    @RestMethod(httpMethod = HttpMethod.POST, path = "/")
    public void test1() {
    }
}
