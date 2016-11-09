package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath7")
@CrossOrigin
public class TestRestFunction7 {
    @RestMethod(httpMethod = HttpMethod.POST, path = "/")
    public String test1() {
        return "test1";
    }
}
