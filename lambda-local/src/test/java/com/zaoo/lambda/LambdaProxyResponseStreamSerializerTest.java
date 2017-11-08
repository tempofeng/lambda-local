package com.zaoo.lambda;

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LambdaProxyResponseStreamSerializerTest {

    private LambdaProxyResponseStreamSerializer serializer;
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        serializer = new LambdaProxyResponseStreamSerializer();
        response = mock(HttpServletResponse.class);
    }

    @Test
    public void deserialize() throws Exception {
        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        String output = "{\"statusCode\":200," +
                "\"headers\":{\"Cache-Control\":\"no-store\",\"Content-Type\":\"application/json\"}," +
                "\"body\":\"{}\"}";
        serializer.deserialize(output.getBytes(), response);
        verify(response).setStatus(200);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).addHeader("Cache-Control", "no-store");
        verify(response).addHeader("Content-Type", "application/json");
        assertThat(writer.toString()).isEqualTo("{}");
    }

}