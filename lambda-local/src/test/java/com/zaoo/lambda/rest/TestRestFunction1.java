package com.zaoo.lambda.rest;

import com.zaoo.lambda.LambdaLocal;

@LambdaLocal("/testRestPath1")
public class TestRestFunction1 {
    @RestMethod(httpMethod = HttpMethod.POST, path = "/")
    public Response hello(@RestParam("firstName") String firstName,
                          @RestParam("lastName") String lastName,
                          @RestHeader("userToken") String userToken) {
        return new Response(firstName, lastName, userToken);
    }

    public static class Response {
        public String firstName;
        public String lastName;
        public String userToken;

        public Response() {
        }

        public Response(String firstName, String lastName, String userToken) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.userToken = userToken;
        }
    }
}
