package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.LambdaLocal;
import com.zaoo.lambda.LambdaLocalContext;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.LambdaProxyResponse;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLambdaRestServiceTest {
    private AbstractLambdaRestService abstractLambdaRestService;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        abstractLambdaRestService = new TestLambdaRestService();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void createMethodInvokers() throws Exception {
        List<MethodInvoker> methodInvokers = abstractLambdaRestService.createMethodInvokers(TestLambdaRestService.class);
        assertThat(methodInvokers.size()).isEqualTo(1);
    }

    @Test
    public void handleRequest() throws Exception {
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHttpMethod("POST");
        req.setPath("/test/");
        req.setHeaders(ImmutableMap.of("Content-Type",
                "application/x-www-form-urlencoded",
                "userToken",
                "testUserToken"));
        req.setQueryStringParameters(ImmutableMap.of("lastName", "feng"));
        req.setBody("firstName=tempo");

        LambdaProxyResponse lambdaProxyResponse = abstractLambdaRestService.handleRequest(req,
                new LambdaLocalContext());
        assertThat(lambdaProxyResponse.getStatusCode()).isEqualTo(200);

        TestLambdaRestService.Response resp = objectMapper.readValue(lambdaProxyResponse.getBody(),
                TestLambdaRestService.Response.class);
        assertThat(resp.firstName).isEqualTo("tempo");
        assertThat(resp.lastName).isEqualTo("feng");
        assertThat(resp.userToken).isEqualTo("testUserToken");

        Map<String, String> headers = lambdaProxyResponse.getHeaders();
        assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, POST, HEAD, OPTIONS");
        assertThat(headers.get("Access-Control-Allow-Headers")).isEqualTo("*");
    }

    @Test
    public void handleRequest_cors_preflight() throws Exception {
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHttpMethod("OPTIONS");
        req.setPath("/test/");
        req.setHeaders(ImmutableMap.of("Access-Control-Request-Method",
                "POST"));

        LambdaProxyResponse resp = abstractLambdaRestService.handleRequest(req,
                new LambdaLocalContext());
        assertThat(resp.getStatusCode()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("{}");
        Map<String, String> headers = resp.getHeaders();
        assertThat(headers.get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, POST, HEAD, OPTIONS");
        assertThat(headers.get("Access-Control-Allow-Headers")).isEqualTo("*");
    }

    @LambdaLocal("/test")
    @CrossOrigin
    private static class TestLambdaRestService extends AbstractLambdaRestService {
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
}