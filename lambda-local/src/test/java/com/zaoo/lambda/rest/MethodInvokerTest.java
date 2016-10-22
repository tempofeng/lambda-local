package com.zaoo.lambda.rest;

import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.LambdaProxyRequest;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodInvokerTest {
    @Test
    public void parsePostParameters() throws Exception {
        Method method = TestRestFunction1.class.getMethod("hello", String.class, String.class, String.class);
        MethodInvoker methodInvoker = new MethodInvoker(method);
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
        Method method = TestRestFunction1.class.getMethod("hello",
                String.class,
                String.class,
                String.class);
        MethodInvoker methodInvoker = new MethodInvoker(method);
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
        Method method = TestRestFunction2.class.getMethods()[0];
        MethodInvoker methodInvoker = new MethodInvoker(method);
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

    @Test
    public void invoke_restParamDeserializer() throws Exception {
        Method method = TestRestFunction3.class.getMethods()[0];
        MethodInvoker methodInvoker = new MethodInvoker(method);
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of(
                "userToken",
                "testUserToken"));
        ImmutableMap<String, String> map = ImmutableMap.<String, String>builder()
                .put("key1", "value1")
                .put("key2", "2")
                .put("key3", "3")
                .put("key4", "4")
                .put("key5", "5")
                .put("key6", "6.6")
                .put("key7", "7.7")
                .put("key8", "8.8")
                .put("key9", "9.9")
                .put("key10", "true")
                .put("key11", "FALSE")
                .put("gender", "MALE")
                .put("customClass", "tempo,feng")
                .build();
        req.setQueryStringParameters(map);
        req.setBody("{\"addr\":\"台北市\",\"mobile\":\"12345\"}");
        TestRestFunction3.Response func = (TestRestFunction3.Response) methodInvoker.invoke(new TestRestFunction3(),
                req);
        assertThat(func.key1).isEqualTo("value1");
        assertThat(func.key2).isEqualTo(2);
        assertThat(func.key3).isEqualTo(3);
        assertThat(func.key4).isEqualTo(4);
        assertThat(func.key5).isEqualTo(5);
        assertThat(func.key6).isEqualTo(6.6f);
        assertThat(func.key7).isEqualTo(7.7f);
        assertThat(func.key8).isEqualTo(8.8d);
        assertThat(func.key9).isEqualTo(9.9d);
        assertThat(func.key10).isEqualTo(true);
        assertThat(func.key11).isEqualTo(false);
        assertThat(func.gender).isEqualTo(TestRestFunction3.Gender.MALE);
        assertThat(func.customClass.firstName).isEqualTo("tempo");
        assertThat(func.customClass.lastName).isEqualTo("feng");
    }
}