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
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction1.class, method, "/testRestPath1");
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
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction1.class, method, "/testRestPath1");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of("Content-Type",
                "application/x-www-form-urlencoded",
                "userToken",
                "testUserToken"));
        req.setQueryStringParameters(ImmutableMap.of("lastName", "feng"));
        req.setBody("firstName=tempo");
        req.setPath("/testRestPath1");
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction1(), req);
        assertThat(result.statusCode).isEqualTo(200);
        assertThat(result.headers).isEmpty();
        TestRestFunction1.Response func = (TestRestFunction1.Response) result.result;
        assertThat(func.firstName).isEqualTo("tempo");
        assertThat(func.lastName).isEqualTo("feng");
        assertThat(func.userToken).isEqualTo("testUserToken");
    }

    @Test
    public void invoke_path() throws Exception {
        Method method = TestRestFunction4.class.getMethod("test3", String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction4.class, method, "/testRestPath4");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath4/test/tempo");
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction4(), req);
        assertThat(result.statusCode).isEqualTo(200);
        assertThat(result.headers).isEmpty();
        String response = (String) result.result;
        assertThat(response).isEqualTo("tempo");
    }

    @Test
    public void invoke_not_required() throws Exception {
        Method method = TestRestFunction5.class.getMethod("hello", String.class, String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction5.class, method, "/testRestPath5");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath5/");
        req.setQueryStringParameters(ImmutableMap.of("firstName", "tempo"));
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction5(), req);
        assertThat(result.statusCode).isEqualTo(200);
        assertThat(result.headers).isEmpty();
        String response = (String) result.result;
        assertThat(response).isEqualTo("tempo,null");
    }

    @Test
    public void invoke_cross_origin() throws Exception {
        Method method = TestRestFunction6.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction6.class, method, "/testRestPath6");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath6/");
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction6(), req);
        assertThat(result.statusCode).isEqualTo(200);

        assertThat(result.headers.size()).isEqualTo(3);
        assertThat(result.headers.get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(result.headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE");
        assertThat(result.headers.get("Access-Control-Allow-Headers")).isEqualTo("*");

        String response = (String) result.result;
        assertThat(response).isEqualTo("test1");
    }

    @Test
    public void invoke_type_cross_origin() throws Exception {
        Method method = TestRestFunction7.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction7.class, method, "/testRestPath7");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath7/");
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction7(), req);
        assertThat(result.statusCode).isEqualTo(200);

        assertThat(result.headers.size()).isEqualTo(3);
        assertThat(result.headers.get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(result.headers.get("Access-Control-Allow-Methods")).isEqualTo("GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE");
        assertThat(result.headers.get("Access-Control-Allow-Headers")).isEqualTo("*");

        String response = (String) result.result;
        assertThat(response).isEqualTo("test1");
    }

    @Test
    public void invoke_required() throws Exception {
        Method method = TestRestFunction5.class.getMethod("hello", String.class, String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction5.class, method, "/testRestPath5");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath5/");
        req.setQueryStringParameters(ImmutableMap.of("lastName", "feng"));

        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction5(), req);

        assertThat(result.statusCode).isEqualTo(500);
        assertThat(((MethodInvoker.Error)result.result).getExceptionClass()).isEqualTo("java.lang.IllegalArgumentException");
    }

    @Test
    public void invoke_body() throws Exception {
        Method method = TestRestFunction2.class.getMethods()[0];
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction2.class, method, "/testRestPath2");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of(
                "userToken",
                "testUserToken"));
        req.setQueryStringParameters(ImmutableMap.of("firstName", "tempo", "lastName", "feng"));
        req.setBody("{\"addr\":\"台北市\",\"mobile\":\"12345\"}");
        req.setPath("/testRestPath2");
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction2(), req);
        assertThat(result.statusCode).isEqualTo(200);
        assertThat(result.headers).isEmpty();
        TestRestFunction2.Response func = (TestRestFunction2.Response) result.result;
        assertThat(func.firstName).isEqualTo("tempo");
        assertThat(func.lastName).isEqualTo("feng");
        assertThat(func.userToken).isEqualTo("testUserToken");
        assertThat(func.addr).isEqualTo("台北市");
        assertThat(func.mobile).isEqualTo("12345");
    }

    @Test
    public void invoke_restParamDeserializer() throws Exception {
        Method method = TestRestFunction3.class.getMethods()[0];
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction3.class, method, "/testRestPath3");
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
        req.setPath("/testRestPath3");
        MethodInvoker.Result result = methodInvoker.invoke(new TestRestFunction3(), req);
        assertThat(result.statusCode).isEqualTo(200);
        assertThat(result.headers).isEmpty();
        TestRestFunction3.Response func = (TestRestFunction3.Response) result.result;
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

    @Test
    public void match() throws Exception {
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath4/test");
        req.setHttpMethod(HttpMethod.GET.name());

        Method method = TestRestFunction4.class.getMethod("test2");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction4.class, method, "/testRestPath4");
        assertThat(methodInvoker.match(req)).isTrue();
    }

    @Test
    public void match_variable() throws Exception {
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath4/test/tempo");
        req.setHttpMethod(HttpMethod.GET.name());

        Method method = TestRestFunction4.class.getMethod("test3", String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction4.class, method, "/testRestPath4");
        assertThat(methodInvoker.match(req)).isTrue();
    }

    @Test
    public void match_root() throws Exception {
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath4");
        req.setHttpMethod(HttpMethod.POST.name());

        Method method = TestRestFunction4.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction4.class, method, "/testRestPath4");
        assertThat(methodInvoker.match(req)).isTrue();
    }
}