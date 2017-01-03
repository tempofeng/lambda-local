package com.zaoo.lambda.rest;

import com.google.common.collect.ImmutableMap;
import com.zaoo.lambda.LambdaProxyRequest;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class MethodInvocationContextTest {
    @Test
    public void parsePostParameters() throws Exception {
        LambdaProxyRequest req = new LambdaProxyRequest();
        req.setHeaders(ImmutableMap.of("Content-Type", "application/x-www-form-urlencoded"));
        req.setBody("key1=value1&key2=value2");

        MethodInvocationContext context = new MethodInvocationContext("/test", "/testRestPath1", req);
        Map<String, String> map = context.parsePostParameters(req);
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.get("key1")).isEqualTo("value1");
        assertThat(map.get("key2")).isEqualTo("value2");
    }
}