package com.zaoo.lambda.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.LambdaProxyRequest;
import com.zaoo.lambda.ObjectMappers;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractLambdaRestServiceTest {
    private final ObjectMapper objectMapper = ObjectMappers.getInstance();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void parsePostParameters() throws Exception {
        Method method = TestRestFunction1.class.getMethod("hello", String.class, String.class, String.class);
        AbstractLambdaRestService.MethodInvoker methodInvoker = new AbstractLambdaRestService.MethodInvoker(objectMapper,
                method);
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded"));
        req.setBody("key1=value1&key2=value2");
        Map<String, String> map = methodInvoker.parsePostParameters(req);
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get("key1")).isEqualTo("value1");
        assertThat(map.get("key2")).isEqualTo("value2");
    }

    @Test
    public void invoke() throws Exception {
        Method method = TestRestFunction1.class.getMethod("hello", String.class, String.class, String.class);
        AbstractLambdaRestService.MethodInvoker methodInvoker = new AbstractLambdaRestService.MethodInvoker(objectMapper,
                method);
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of("Content-Type",
                "application/x-www-form-urlencoded",
                "userToken",
                "testUserToken"));
        req.setQueryStringParameters(ImmutableMap.of("lastName", "feng"));
        req.setBody("firstName=tempo");
        TestRestFunction1.Response func = (TestRestFunction1.Response) methodInvoker.invoke(new TestRestFunction1(),
                req);
        assertThat(func.firstName).isEqualTo("tempo");
        assertThat(func.lastName).isEqualTo("feng");
        assertThat(func.userToken).isEqualTo("testUserToken");
    }

    @Test
    public void invoke_body() throws Exception {
        Method method = TestRestFunction2.class.getMethod("hello", String.class, String.class, String.class,
                TestRestFunction2.Request.class);
        AbstractLambdaRestService.MethodInvoker methodInvoker = new AbstractLambdaRestService.MethodInvoker(objectMapper,
                method);
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of(
                "userToken",
                "testUserToken"));
        req.setQueryStringParameters(ImmutableMap.of("firstName", "tempo", "lastName", "feng"));
        req.setBody("{\"addr\":\"台北市\",\"mobile\":\"12345\"}");
        TestRestFunction2.Response func = (TestRestFunction2.Response) methodInvoker.invoke(new TestRestFunction2(),
                req);
        assertThat(func.firstName).isEqualTo("tempo");
        assertThat(func.lastName).isEqualTo("feng");
        assertThat(func.userToken).isEqualTo("testUserToken");
        assertThat(func.addr).isEqualTo("台北市");
        assertThat(func.mobile).isEqualTo("12345");
    }
}