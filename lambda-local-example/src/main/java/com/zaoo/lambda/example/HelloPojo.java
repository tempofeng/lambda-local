package com.zaoo.lambda.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaRequestDeserializer;
import com.zaoo.lambda.LambdaResponseSerializer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@LambdaLocal(value = "/lleHelloPojo", serializer = HelloPojo.Serializer.class, deserializer = HelloPojo.Deserializer.class)
@SuppressWarnings("unused")
public class HelloPojo implements RequestHandler<HelloPojo.RequestClass, HelloPojo.ResponseClass> {

    @Override
    public ResponseClass handleRequest(RequestClass request, Context context) {
        String greetingString = String.format("Hello %s, %s.", request.firstName, request.lastName);
        return new ResponseClass(greetingString);
    }

    public static class RequestClass {
        String firstName;
        String lastName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public RequestClass(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public RequestClass() {
        }
    }

    public static class ResponseClass {
        String greetings;

        public String getGreetings() {
            return greetings;
        }

        public void setGreetings(String greetings) {
            this.greetings = greetings;
        }

        public ResponseClass(String greetings) {
            this.greetings = greetings;
        }

        public ResponseClass() {
        }
    }

    public static class Deserializer implements LambdaRequestDeserializer<RequestClass> {
        @Override
        public RequestClass serialize(HttpServletRequest req) throws IOException {
            return new RequestClass(req.getParameter("firstName"), req.getParameter("lastName"));
        }
    }

    public static class Serializer implements LambdaResponseSerializer<ResponseClass> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public void deserialize(ResponseClass output, HttpServletResponse resp) throws IOException {
            objectMapper.writeValue(resp.getOutputStream(), output);
        }
    }
}
