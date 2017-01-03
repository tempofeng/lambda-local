package com.zaoo.lambda.rest;

import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.LambdaProxyRequest;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodInvokerTest {
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
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction1(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders()).isEmpty();
        TestRestFunction1.Response func = (TestRestFunction1.Response) result.getResult();
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
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction4(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders()).isEmpty();
        String response = (String) result.getResult();
        assertThat(response).isEqualTo("tempo");
    }

    @Test
    public void invoke_not_required() throws Exception {
        Method method = TestRestFunction5.class.getMethod("hello", String.class, String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction5.class, method, "/testRestPath5");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath5/");
        req.setQueryStringParameters(ImmutableMap.of("firstName", "tempo"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction5(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders()).isEmpty();
        String response = (String) result.getResult();
        assertThat(response).isEqualTo("tempo,null");
    }

    @Test
    public void invoke_cross_origin() throws Exception {
        Method method = TestRestFunction6.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction6.class, method, "/testRestPath6");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath6/");
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction6(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        assertThat(result.getHeaders().size()).isEqualTo(3);
        assertThat(result.getHeaders().get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(result.getHeaders().get("Access-Control-Allow-Methods")).isEqualTo(
                "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE");
        assertThat(result.getHeaders().get("Access-Control-Allow-Headers")).isEqualTo("*");

        String response = (String) result.getResult();
        assertThat(response).isEqualTo("test1");
    }

    @Test
    public void invoke_cross_origin_allow_headers() throws Exception {
        Method method = TestRestFunction6.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction6.class, method, "/testRestPath6");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath6/");
        req.setHeaders(ImmutableMap.of("Access-Control-Request-Headers", "Content-Type"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction6(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        assertThat(result.getHeaders().size()).isEqualTo(3);
        assertThat(result.getHeaders().get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(result.getHeaders().get("Access-Control-Allow-Methods")).isEqualTo(
                "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE");
        assertThat(result.getHeaders().get("Access-Control-Allow-Headers")).isEqualTo("Content-Type");

        String response = (String) result.getResult();
        assertThat(response).isEqualTo("test1");
    }

    @Test
    public void invoke_type_cross_origin() throws Exception {
        Method method = TestRestFunction7.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction7.class, method, "/testRestPath7");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath7/");
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction7(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        assertThat(result.getHeaders().size()).isEqualTo(3);
        assertThat(result.getHeaders().get("Access-Control-Allow-Origin")).isEqualTo("*");
        assertThat(result.getHeaders().get("Access-Control-Allow-Methods")).isEqualTo(
                "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE");
        assertThat(result.getHeaders().get("Access-Control-Allow-Headers")).isEqualTo("*");

        String response = (String) result.getResult();
        assertThat(response).isEqualTo("test1");
    }

    @Test
    public void invoke_restResponseEntity() throws Exception {
        Method method = TestRestFunction9.class.getMethod("test1");
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction9.class, method, "/testRestPath9");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath9/");
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction9(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders().isEmpty()).isTrue();
        String response = (String) result.getResult();
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    public void invoke_lambdaProxyRequest() throws Exception {
        Method method = TestRestFunction10.class.getMethod("test1", String.class, LambdaProxyRequest.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction10.class, method, "/testRestPath10");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath10/");
        req.setQueryStringParameters(ImmutableMap.of("test1", "Hi"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction10(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders().isEmpty()).isTrue();
        String response = (String) result.getResult();
        assertThat(response).isEqualTo("/testRestPath10/");
    }

    @Test
    public void invoke_cookie() throws Exception {
        Method method = TestRestFunction11.class.getMethod("test1", String.class, String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction11.class, method, "/testRestPath11");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath11/");
        req.setHeaders(ImmutableMap.of("Cookie", "test1=Hello;"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction11(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders().isEmpty()).isTrue();
        String response = (String) result.getResult();
        assertThat(response).isEqualTo("Hello");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void invoke_type_json_param1() throws Exception {
        Method method = TestRestFunction8.class.getMethod("test1", List.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction8.class, method, "/testRestPath8");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath8/test1");
        req.setQueryStringParameters(ImmutableMap.of("test", "[\"test1\",\"test2\"]"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction8(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        List<String> response = (List<String>) result.getResult();
        assertThat(response).contains("test1", "test2");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void invoke_type_json_param2() throws Exception {
        Method method = TestRestFunction8.class.getMethod("test2", Map.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction8.class, method, "/testRestPath8");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath8/test2");
        req.setQueryStringParameters(ImmutableMap.of("test", "{\"key1\":\"value1\",\"key2\":\"value2\"}"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction8(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        Map<String, String> response = (Map<String, String>) result.getResult();
        assertThat(response).containsAllEntriesOf(ImmutableMap.of("key1", "value1", "key2", "value2"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void invoke_type_json_param3() throws Exception {
        Method method = TestRestFunction8.class.getMethod("test3", TestRestFunction8.TestClass.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction8.class, method, "/testRestPath8");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath8/test3");
        req.setQueryStringParameters(ImmutableMap.of("test", "{\"str\":\"test1\"}"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction8(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        TestRestFunction8.TestClass response = (TestRestFunction8.TestClass) result.getResult();
        assertThat(response).isEqualTo(new TestRestFunction8.TestClass("test1"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void invoke_type_json_param4() throws Exception {
        Method method = TestRestFunction8.class.getMethod("test4", List.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction8.class, method, "/testRestPath8");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath8/test4");
        req.setQueryStringParameters(ImmutableMap.of("test", "[{\"str\":\"test1\"}]"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction8(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        List<TestRestFunction8.TestClass> response = (List<TestRestFunction8.TestClass>) result.getResult();
        assertThat(response).contains(new TestRestFunction8.TestClass("test1"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void invoke_type_json_param5() throws Exception {
        Method method = TestRestFunction8.class.getMethod("test5", Map.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction8.class, method, "/testRestPath8");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath8/test5");
        req.setQueryStringParameters(ImmutableMap.of("test", "{\"key1\":{\"str\":\"test1\"}}"));
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction8(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);

        Map<String, TestRestFunction8.TestClass> response = (Map<String, TestRestFunction8.TestClass>) result.getResult();
        assertThat(response).containsAllEntriesOf(ImmutableMap.of("key1", new TestRestFunction8.TestClass("test1")));
    }

    @Test
    public void invoke_required() throws Exception {
        Method method = TestRestFunction5.class.getMethod("hello", String.class, String.class);
        MethodInvoker methodInvoker = new MethodInvoker(TestRestFunction5.class, method, "/testRestPath5");
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setPath("/testRestPath5/");
        req.setQueryStringParameters(ImmutableMap.of("lastName", "feng"));

        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction5(), req);

        assertThat(result.getStatusCode()).isEqualTo(500);
        assertThat(((MethodInvoker.Error) result.getResult()).getExceptionClass()).isEqualTo(
                "java.lang.IllegalArgumentException");
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
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction2(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders()).isEmpty();
        TestRestFunction2.Response func = (TestRestFunction2.Response) result.getResult();
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
        RestResponseEntity result = methodInvoker.invoke(new TestRestFunction3(), req);
        assertThat(result.getStatusCode()).isEqualTo(200);
        assertThat(result.getHeaders()).isEmpty();
        TestRestFunction3.Response func = (TestRestFunction3.Response) result.getResult();
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