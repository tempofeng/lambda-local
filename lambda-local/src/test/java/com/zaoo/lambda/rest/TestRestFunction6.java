package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath6")
public class TestRestFunction6 {
    @RestMethod(httpMethod = HttpMethod.POST, path = "/")
    @CrossOrigin
    public String test1() {
        return "test1";
    }
}
