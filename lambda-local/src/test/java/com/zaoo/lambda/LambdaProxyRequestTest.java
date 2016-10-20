package com.zaoo.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LambdaProxyRequestTest {
    @Test
    public void newInstance() throws IOException {
        String req = "{\n" +
                "    \"resource\": \"/lleHelloLambdaProxy\",\n" +
                "    \"path\": \"/lleHelloLambdaProxy\",\n" +
                "    \"httpMethod\": \"GET\",\n" +
                "    \"headers\": null,\n" +
                "    \"queryStringParameters\": null,\n" +
                "    \"pathParameters\": null,\n" +
                "    \"stageVariables\": null,\n" +
                "    \"requestContext\": {\n" +
                "        \"accountId\": \"145798268361\",\n" +
                "        \"resourceId\": \"87oybh\",\n" +
                "        \"stage\": \"test-invoke-stage\",\n" +
                "        \"requestId\": \"test-invoke-request\",\n" +
                "        \"identity\": {\n" +
                "            \"cognitoIdentityPoolId\": null,\n" +
                "            \"accountId\": \"145798268361\",\n" +
                "            \"cognitoIdentityId\": null,\n" +
                "            \"caller\": \"145798268361\",\n" +
                "            \"apiKey\": \"test-invoke-api-key\",\n" +
                "            \"sourceIp\": \"test-invoke-source-ip\",\n" +
                "            \"cognitoAuthenticationType\": null,\n" +
                "            \"cognitoAuthenticationProvider\": null,\n" +
                "            \"userArn\": \"arn:aws:iam::145798268361:root\",\n" +
                "            \"userAgent\": \"Apache-HttpClient/4.5.x (Java/1.8.0_102)\",\n" +
                "            \"user\": \"145798268361\"\n" +
                "        },\n" +
                "        \"resourcePath\": \"/lleHelloLambdaProxy\",\n" +
                "        \"httpMethod\": \"GET\",\n" +
                "        \"apiId\": \"yp163bjalg\"\n" +
                "    },\n" +
                "    \"body\": null\n" +
                "}";
        ObjectMapper objectMapper = new ObjectMapper();
        LambdaProxyRequest request = objectMapper.readValue(req, LambdaProxyRequest.class);
        assertThat(request.getHeaders()).isNotNull();
        assertThat(request.getHeaders().get("test")).isNull();
    }
}