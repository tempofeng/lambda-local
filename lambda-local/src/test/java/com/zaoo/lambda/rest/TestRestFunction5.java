package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath5")
public class TestRestFunction5 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public String hello(@RestParam("firstName") String firstName,
                        @RestParam(value = "lastName", required = false) String lastName) {
        return String.format("%s,%s", firstName, lastName);
    }
}
