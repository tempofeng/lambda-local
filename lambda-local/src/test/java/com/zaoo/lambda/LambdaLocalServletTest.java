package com.zaoo.lambda;

import com.zaoo.lambda.test.TestFunction1;
import com.zaoo.lambda.test.TestFunction2;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.reflections.ReflectionUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class LambdaLocalServletTest {
    private LambdaLocalServlet lambdaLocalServlet;

    @Before
    public void setUp() throws Exception {
        lambdaLocalServlet = new LambdaLocalServlet();
    }

    @Test
    public void invokeLambdaFunction_testFunction1() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("tempo".getBytes());
        MockHttpServletResponse resp = new MockHttpServletResponse();
        lambdaLocalServlet.invokeLambdaFunction(req,
                resp,
                new LambdaFunction("/testPath1",
                        TestFunction1.class,
                        null,
                        new StringDeserializer(),
                        new StringSerializer()));
        assertThat(resp.getContentAsString()).isEqualTo("hello tempo");
    }

    @Test
    public void invokeLambdaFunction_testFunction2_method1() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("tempo".getBytes());
        MockHttpServletResponse resp = new MockHttpServletResponse();
        lambdaLocalServlet.invokeLambdaFunction(req,
                resp,
                new LambdaFunction("/testPath2",
                        TestFunction2.class,
                        ReflectionUtils.getMethods(TestFunction2.class,
                                method -> method.getName().equals("method1")).stream()
                                .findFirst()
                                .get(),
                        new StringDeserializer(),
                        new StringSerializer()));
        assertThat(resp.getContentAsString()).isEqualTo("hello tempo");
    }

    @Test
    public void invokeLambdaFunction_testFunction2_method2() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setContent("tempo".getBytes());
        MockHttpServletResponse resp = new MockHttpServletResponse();
        lambdaLocalServlet.invokeLambdaFunction(req,
                resp,
                new LambdaFunction("/testPath3",
                        TestFunction2.class,
                        ReflectionUtils.getMethods(TestFunction2.class,
                                method -> method.getName().equals("method2")).stream()
                                .findFirst()
                                .get(),
                        new StringDeserializer(),
                        new StringSerializer()));
        assertThat(resp.getContentAsString()).isEqualTo("hi tempo");
    }

    private static class StringDeserializer implements LambdaRequestDeserializer<String> {
        @Override
        public String serialize(HttpServletRequest req) throws IOException {
            return IOUtils.toString(req.getInputStream(), "UTF-8");
        }
    }

    private static class StringSerializer implements LambdaResponseSerializer<String> {
        @Override
        public void deserialize(String output, HttpServletResponse resp) throws IOException {
            IOUtils.write(output, resp.getOutputStream(), "UTF-8");
        }
    }
}