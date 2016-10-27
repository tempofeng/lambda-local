package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath5")
public class TestRestFunction5 {
    @RestMethod(httpMethod = HttpMethod.GET, path = "/")
    public String hello(@RestQuery("firstName") String firstName,
                        @RestQuery(value = "lastName", required = false) String lastName) {
        return String.format("%s,%s", firstName, lastName);
    }
}
