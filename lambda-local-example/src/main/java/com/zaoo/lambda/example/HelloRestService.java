package com.zaoo.lambda.example;

import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.rest.*;

@LambdaLocal(path = "/helloRestService")
@SuppressWarnings("unused")
public class HelloRestService extends AbstractLambdaRestService {

    @RestMethod(HttpMethod.POST)
    public HelloPojo.ResponseClass hello(@RestBody RequestClass request) {
        String greetingString = String.format("Hello %s, %s.", request.getFirstName(), request.getLastName());
        return new HelloPojo.ResponseClass(greetingString);
    }

    @RestMethod(HttpMethod.GET)
    public HelloPojo.ResponseClass helloWithRequest(@RestQuery("firstName") String firstName,
                                                    @RestQuery("lastName") String lastName,
                                                    LambdaProxyRequest request) {
        String greetingString = String.format("Hello %s, %s from %s!",
                firstName,
                lastName,
                request.getRequestContext().getIdentity().getSourceIp());
        return new HelloPojo.ResponseClass(greetingString);
    }

    static class RequestClass {
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

        RequestClass(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        RequestClass() {
        }
    }

    static class ResponseClass {
        String greetings;

        public String getGreetings() {
            return greetings;
        }

        public void setGreetings(String greetings) {
            this.greetings = greetings;
        }

        ResponseClass(String greetings) {
            this.greetings = greetings;
        }

        ResponseClass() {
        }
    }
}
